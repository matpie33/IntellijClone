package core.backend;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import core.dto.ApplicatonState;
import core.dto.ClassStructureDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ClassStructureParser {

    private final Node.TreeTraversal treeTraversal = Node.TreeTraversal.PREORDER;
    private ApplicatonState applicatonState;


    public ClassStructureParser(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public boolean parseClassStructure(File file){
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            ClassStructureDTO classStructureDTO = new ClassStructureDTO();
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
        }
        return false;
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
    }

    private Map<String, List<Range>> getVariablesHidingFields(CompilationUnit cu, Set<String> fieldNames) {
        Map<String, List<Range>> variablesHidingFields = new HashMap<>();

        for (VariableDeclarationExpr variableDeclaration : cu.findAll(VariableDeclarationExpr.class, treeTraversal)) {
            Range variableScope = getVariableScope(variableDeclaration);
            NodeList<VariableDeclarator> variables = variableDeclaration.getVariables();
            checkForVariablesHidingFields(fieldNames, variablesHidingFields, variableScope, variables);
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
            String name = variable.getNameAsString();
            if (fieldNames.contains(name)){
                variablesHidingFields.putIfAbsent(name, new ArrayList<>());
                variablesHidingFields.get(name).add(variableScope);
            }
        }
    }

    private Range getVariableScope(VariableDeclarationExpr variableDeclaration) {
        Range variableScope;
        Node parentNode = variableDeclaration.getParentNode().get();
        if (parentNode instanceof ForStmt){
            ForStmt forStatement = (ForStmt) parentNode;
            variableScope = forStatement.getRange().get();
        }
        else if (parentNode instanceof ExpressionStmt){
            Node node = parentNode.getParentNode().get();
            if (node instanceof BlockStmt){
                BlockStmt blockStmt = (BlockStmt) node;
                variableScope = blockStmt.getRange().get();
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
                Range range = variableDeclarator.getRange().get();
                String variableName = variableDeclarator.getNameAsString();
                fieldNames.add(variableName);
                classStructureDTO.addFieldAccess(range);
            }
        }
        return fieldNames;
    }


}
