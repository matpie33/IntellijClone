package root.core.classmanipulating;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;
import root.core.codecompletion.ClassNamesCollector;
import root.core.constants.ClassType;
import root.core.dto.ApplicationState;
import root.core.dto.ClassStructureDTO;
import root.core.jdk.manipulating.JavaSourcesExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class ClassStructureParser {

    public static final String PACKAGE_KEYWORD = "package ";
    private final Node.TreeTraversal treeTraversal = Node.TreeTraversal.PREORDER;
    private ApplicationState applicationState;

    private ClassNamesCollector classNamesCollector;

    private JavaSourcesExtractor javaSourcesExtractor;


    public ClassStructureParser(ApplicationState applicationState, ClassNamesCollector classNamesCollector, JavaSourcesExtractor javaSourcesExtractor) {
        this.applicationState = applicationState;
        this.classNamesCollector = classNamesCollector;
        this.javaSourcesExtractor = javaSourcesExtractor;
    }

    public void parseClassContent (String content, File file, ClassOrigin origin){
        CompilationUnit compilationUnit = StaticJavaParser.parse(content);
        ClassStructureDTO classStructureDTO = extractStructureFromCompilationUnit(compilationUnit, origin, "");
        applicationState.putClassStructure(file, classStructureDTO);
    }

    public boolean parseClassStructure(File file, ClassOrigin origin){
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            String rootDirectory = "";
            if (origin.equals(ClassOrigin.JDK)){
                Path javaSourcesDirectory = javaSourcesExtractor.getJavaSourcesDirectory();
                Path fileRelativeToJavaSources = javaSourcesDirectory.relativize(file.toPath());
                Path rootDirectoryObject = fileRelativeToJavaSources.subpath(0, 1);
                rootDirectory = rootDirectoryObject + "/";
            }
            ClassStructureDTO classStructureDTO = extractStructureFromCompilationUnit(cu, origin,rootDirectory);
            if (classStructureDTO == null) {
                return false;
            }

            applicationState.putClassStructure(file, classStructureDTO);
            return containsMainMethod(file, cu);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (ParseProblemException ex){
            applicationState.addClassWithCompilationError(file);
            try {
                int lineNumber = findPackageDeclarationLine(file);
                ClassStructureDTO classStructureDTO = new ClassStructureDTO();
                classStructureDTO.setPackageDeclarationPosition(new Position(lineNumber, 0));
                applicationState.putClassStructure(file, classStructureDTO);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private ClassStructureDTO extractStructureFromCompilationUnit(CompilationUnit cu, ClassOrigin origin, String rootDirectory) {
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        if (types.isEmpty()){
            if (cu.getPackageDeclaration().isPresent()){
                ClassStructureDTO classStructureDTO = new ClassStructureDTO();
                classStructureDTO.setClassType(ClassType.PACKAGE_DECLARATION);
                return classStructureDTO;
            }
            else if (cu.getModule().isPresent()){
                ClassStructureDTO classStructureDTO = new ClassStructureDTO();
                classStructureDTO.setClassType(ClassType.MODULE_DECLARATION);
                return classStructureDTO;
            }
            else{
                return null;
            }
        }
        TypeDeclaration<?> typeDeclaration = types.get(0);
        ClassStructureDTO classStructureDTO = new ClassStructureDTO();
        cu.getImports().forEach(importDeclaration -> classStructureDTO.addImport(importDeclaration.getNameAsString()));
        if (typeDeclaration instanceof ClassOrInterfaceDeclaration){
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) typeDeclaration;
            ClassType classType = getClassType(classOrInterfaceDeclaration);
            classStructureDTO.setClassType(classType);
            Position declarationPosition = classOrInterfaceDeclaration.getName().getRange().get().begin;
            classStructureDTO.setClassDeclarationPosition(declarationPosition);
            String packageName = getPackageName(cu, classStructureDTO);
            classNamesCollector.addClassIfAccessible(classOrInterfaceDeclaration, packageName, origin, rootDirectory);
        }
        else if (typeDeclaration instanceof EnumDeclaration){
            EnumDeclaration enumDeclaration = (EnumDeclaration) typeDeclaration;
            classStructureDTO.setClassType(ClassType.ENUM);
            String packageName = getPackageName(cu, classStructureDTO);
            classNamesCollector.addClassIfAccessible(enumDeclaration, packageName, origin, rootDirectory);
        }
        else if (typeDeclaration instanceof AnnotationDeclaration){
            AnnotationDeclaration annotationDeclaration = (AnnotationDeclaration) typeDeclaration;
            classStructureDTO.setClassType(ClassType.ANNOTATION);
            String packageName = getPackageName(cu, classStructureDTO);
            classNamesCollector.addClassIfAccessible(annotationDeclaration, packageName, origin, rootDirectory);
        }
        getCommentsSections(cu, classStructureDTO);
        Set<String> fieldNames = getFieldNames(cu, classStructureDTO);
        Map<String, List<Range>> variablesHidingFields = getVariablesHidingFields(cu, fieldNames);
        checkForFieldsAccess(cu, classStructureDTO, fieldNames, variablesHidingFields);
        return classStructureDTO;
    }

    private String getPackageName(CompilationUnit cu, ClassStructureDTO classStructureDTO) {
        Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
        String packageName = "";
        if (packageDeclaration.isPresent()){
            PackageDeclaration declaration = packageDeclaration.get();
            Range range = declaration.getRange().get();
            classStructureDTO.setPackageDeclarationPosition(range.begin);
            packageName = declaration.getNameAsString();
        }
        return packageName;
    }

    private ClassType getClassType(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassType classType;
        if (classOrInterfaceDeclaration.isInterface()){
            classType = ClassType.INTERFACE;
        }
        else if (classOrInterfaceDeclaration.isEnumDeclaration()){
            classType = ClassType.ENUM;
        }
        else{
            classType = ClassType.CLASS;
        }
        return classType;
    }

    private void getCommentsSections(CompilationUnit cu, ClassStructureDTO classStructureDTO) {
        for (Comment lineComment : cu.getAllComments()) {
            Range range = lineComment.getRange().get();
            classStructureDTO.addCommentRange(range);
        }
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
