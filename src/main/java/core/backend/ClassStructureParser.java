    package core.backend;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import core.dto.ApplicatonState;
import core.dto.ClassStructureDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
public class ClassStructureParser {

    public static final String PACKAGE_KEYWORD = "package ";
    private final Node.TreeTraversal treeTraversal = Node.TreeTraversal.PREORDER;
    private ApplicatonState applicatonState;


    public ClassStructureParser(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public boolean parseClassStructure(File file){
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            NodeList<TypeDeclaration<?>> types = cu.getTypes();
            if (types.isEmpty()){
                return false;
            }
            TypeDeclaration<?> typeDeclaration = types.get(0);
            ClassStructureDTO classStructureDTO = new ClassStructureDTO();
            cu.getImports().forEach(importDeclaration -> classStructureDTO.addImport(importDeclaration.getNameAsString()));
            if (typeDeclaration instanceof ClassOrInterfaceDeclaration){
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) typeDeclaration;
                String name = classOrInterfaceDeclaration.getNameAsString();
                Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
                String packageName = "";
                if (packageDeclaration.isPresent()){
                    PackageDeclaration declaration = packageDeclaration.get();
                    Range range = declaration.getRange().get();
                    classStructureDTO.setPackageDeclarationPosition(range.begin);
                    packageName = declaration.getNameAsString();
                }
                applicatonState.addClassWithPackage(name, packageName);
            }
            Set<String> fieldNames = getFieldNames(cu, classStructureDTO);
            Map<String, List<Range>> variablesHidingFields = getVariablesHidingFields(cu, fieldNames);
            checkForFieldsAccess(cu, classStructureDTO, fieldNames, variablesHidingFields);
            boolean hasMainMethod = containsMainMethod(file, cu);

            applicatonState.putClassStructure(file, classStructureDTO);
            return hasMainMethod;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (ParseProblemException ex){
            applicatonState.addClassWithCompilationError(file);
            try {
                int lineNumber = findPackageDeclarationLine(file);
                ClassStructureDTO classStructureDTO = new ClassStructureDTO();
                classStructureDTO.setPackageDeclarationPosition(new Position(lineNumber, 0));
                applicatonState.putClassStructure(file, classStructureDTO);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private int findPackageDeclarationLine(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int lineNumber = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(PACKAGE_KEYWORD)){
                lineNumber = i;
                break;
            }
        }
        return lineNumber;
    }

    private boolean containsMainMethod(File file, CompilationUnit cu) {
        boolean hasMainMethod = false;
        for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class, treeTraversal)) {
            if (isMethodSignatureMatchingMain(methodDeclaration)) {
                boolean isMainMethod = checkIfMethodHas1ArrayParameterOfTypeString(file, methodDeclaration);
                if (isMainMethod) {
                    hasMainMethod = true;
                }
            }
        }
        return hasMainMethod;
    }

    private boolean isMethodSignatureMatchingMain(MethodDeclaration method) {
        return method.isStatic() && method.isPublic() && method.getType().isVoidType();
    }

    private boolean checkIfMethodHas1ArrayParameterOfTypeString(File file, MethodDeclaration method) {
        NodeList<Parameter> parameters = method.getParameters();
        if (parameters.size() ==1){
            Parameter parameter = parameters.iterator().next();
            if (parameter.getType().isArrayType() && parameter.getType().getElementType().toString().equals("String")){
                return true;
            }
        }
        return false;
    }

    private void checkForFieldsAccess(CompilationUnit cu, ClassStructureDTO classStructureDTO, Set<String> fieldNames, Map<String, List<Range>> variablesHidingFields) {
        for (NameExpr nameExpr : cu.findAll(NameExpr.class, treeTraversal)) {
            String variableName = nameExpr.getNameAsString();
            if (!fieldNames.contains(variableName)){
                continue;
            }
            List<Range> scopesOfVariablesHidingFields = variablesHidingFields.getOrDefault(variableName, new ArrayList<>());
            boolean isVariable = isVariable(nameExpr, scopesOfVariablesHidingFields);
            if (!isVariable){
                classStructureDTO.addFieldAccess(nameExpr.getRange().get());
            }
        }
        for (FieldAccessExpr fieldAccessExpr : cu.findAll(FieldAccessExpr.class)) {
            SimpleName simpleName = fieldAccessExpr.getChildNodes().stream().filter(SimpleName.class::isInstance).map(SimpleName.class::cast).findFirst().orElseThrow();
            classStructureDTO.addFieldAccess(simpleName.getRange().get());
        }
    }

    private Map<String, List<Range>> getVariablesHidingFields(CompilationUnit cu, Set<String> fieldNames) {
        Map<String, List<Range>> variablesHidingFields = new HashMap<>();

        for (VariableDeclarationExpr variableDeclaration : cu.findAll(VariableDeclarationExpr.class, treeTraversal)) {
            Range variableScope = getVariableScope(variableDeclaration);
            NodeList<VariableDeclarator> variables = variableDeclaration.getVariables();
            checkForVariablesHidingFields(fieldNames, variablesHidingFields, variableScope, variables);
        }
        for (Parameter parameter : cu.findAll(Parameter.class)) {
            Node parentNode = parameter.getParentNode().get();
            if (parentNode instanceof ConstructorDeclaration || parentNode instanceof MethodDeclaration){
                Range parameterRange = parentNode.getRange().get();
                String name = parameter.getNameAsString();
                checkIfVariableHidesField(fieldNames, variablesHidingFields, parameterRange, name);
            }
        }
        return variablesHidingFields;
    }

    private boolean isVariable(NameExpr nameExpr, List<Range> scopes) {
        int line = nameExpr.getRange().get().begin.line;
        boolean isVariable = false;
        for (Range scope : scopes) {
            if (scope.begin.line<= line && scope.end.line>= line){
                isVariable = true;
                break;
            }
        }
        return isVariable;
    }

    private void checkForVariablesHidingFields(Set<String> fieldNames, Map<String, List<Range>> variablesHidingFields, Range variableScope, NodeList<VariableDeclarator> variables) {
        for (VariableDeclarator variable : variables) {
            checkIfVariableHidesField(fieldNames, variablesHidingFields, variableScope, variable.getNameAsString());
        }
    }

    private void checkIfVariableHidesField(Set<String> fieldNames, Map<String, List<Range>> variablesHidingFields, Range variableScope, String name) {
        if (fieldNames.contains(name)){
            variablesHidingFields.putIfAbsent(name, new ArrayList<>());
            variablesHidingFields.get(name).add(variableScope);
        }
    }

    private Range getVariableScope(VariableDeclarationExpr variableDeclaration) {
        Range variableScope;
        Node parentNode = variableDeclaration.getParentNode().get();
        if (parentNode instanceof ForStmt || parentNode instanceof ForEachStmt || parentNode instanceof TryStmt){
            variableScope = parentNode.getRange().get();
        }
        else if (parentNode instanceof ExpressionStmt){
            Node node = parentNode.getParentNode().get();
            if (node instanceof BlockStmt || node instanceof SwitchEntry){
                variableScope = node.getRange().get();
            }
            else{
                throw new UnsupportedOperationException();
            }
        }
        else{
            throw new UnsupportedOperationException();
        }
        return variableScope;
    }

    private Set<String> getFieldNames(CompilationUnit cu, ClassStructureDTO classStructureDTO) {
        Set<String> fieldNames = new HashSet<>();
        for (FieldDeclaration fieldDeclaration : cu.findAll(FieldDeclaration.class, treeTraversal)) {
            for (VariableDeclarator variableDeclarator : fieldDeclaration.getVariables()) {
                Range range = variableDeclarator.getName().getRange().get();
                String variableName = variableDeclarator.getNameAsString();
                fieldNames.add(variableName);
                classStructureDTO.addFieldAccess(range);
            }
        }
        return fieldNames;
    }


}
