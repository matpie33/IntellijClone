package core.backend;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import core.dto.ClassStructureDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClassStructureVisitor extends VoidVisitorAdapter<Object> {

    private Map<String, Integer> localVariablesHidingFieldsNameToLineMap = new HashMap<>();

    private ClassStructureDTO classStructure;

    private Map<String, Range> fields = new HashMap<>();

    private File file;

    private boolean hasMainMethod;


    public void visitFile (File file) throws FileNotFoundException {
        this.file = file;
        localVariablesHidingFieldsNameToLineMap.clear();
        hasMainMethod = false;
        fields.clear();
        classStructure = new ClassStructureDTO();
        visit(StaticJavaParser.parse(file), null);
    }

    public ClassStructureDTO getClassStructure() {
        return classStructure;
    }

    public boolean hasMainMethod() {
        return hasMainMethod;
    }

    @Override
    public void visit(FieldDeclaration field, Object arg) {
        super.visit(field, arg);
        for (VariableDeclarator variable : field.getVariables()) {
            String name = variable.getNameAsString();
            Range range = variable.getRange().orElseThrow();
            fields.put(name, range);
            classStructure.addFieldAccess(range);
        }
    }

    @Override
    public void visit(FieldAccessExpr target, Object arg) {
        super.visit(target, arg);
        Range range = target.getName().getRange().get();
        classStructure.addFieldAccess(range);
    }

    @Override
    public void visit(NameExpr nameExpr, Object arg) {
        super.visit(nameExpr, arg);
        String name = nameExpr.getNameAsString();
        Range range = nameExpr.getRange().get();
        if (fields.containsKey(name) && (!localVariablesHidingFieldsNameToLineMap.containsKey(name) ||
                localVariablesHidingFieldsNameToLineMap.get(name) > range.begin.line)) {
            classStructure.addFieldAccess(range);
        }
    }

    @Override
    public void visit(VariableDeclarationExpr variableDeclarationExpr, Object arg) {
        super.visit(variableDeclarationExpr, arg);
        for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
            String name = variable.getNameAsString();
            Range range = variable.getRange().orElseThrow();
            int line = range.begin.line;
            if (fields.containsKey(name)) {
                localVariablesHidingFieldsNameToLineMap.put(name, line);
            }
        }
    }

    @Override
    public void visit(MethodDeclaration method, Object arg) {
        super.visit(method, arg);
        localVariablesHidingFieldsNameToLineMap.clear();
        if (isMethodSignatureMatchingMain(method)) {
            boolean isMainMethod = checkIfMethodHas1ArrayParameterOfTypeString(file, method);
            if (isMainMethod) {
                hasMainMethod = true;
            }
        }
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

}
