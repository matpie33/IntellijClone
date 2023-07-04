package core.uibuilders;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.Type;
import core.dto.*;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClassStructureBuilderUI {

    public DefaultMutableTreeNode build (ClassOrInterfaceDeclaration classOrInterfaceDeclaration){
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(classOrInterfaceDeclaration.getName());
        for (FieldDeclaration fieldDeclaration : classOrInterfaceDeclaration.getFields()) {
            addFieldNode(top, fieldDeclaration);
        }
        for (MethodDeclaration methodDeclaration : classOrInterfaceDeclaration.getMethods()) {
            addMethodNode(top, methodDeclaration);
        }
        return top;

    }

    private void addFieldNode(DefaultMutableTreeNode parentNode, FieldDeclaration fieldSignature) {

        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(String.format("%s %s:%s",getModifiers(fieldSignature),
                getFieldNames(fieldSignature), getType(fieldSignature)));
        parentNode.add(fieldNode);
    }

    private Type getType(FieldDeclaration fieldSignature) {
        return fieldSignature.getVariables().stream().map(VariableDeclarator::getType).findFirst().orElseThrow();
    }

    private String getFieldNames(FieldDeclaration fieldSignature) {
        return fieldSignature.getVariables().stream().map(variableDeclarator -> variableDeclarator.getName().toString()).collect(Collectors.joining(", "));
    }

    private String getModifiers(FieldDeclaration fieldSignature) {
        return fieldSignature.getModifiers().stream().map(modifier -> modifier.getKeyword().asString()).collect(Collectors.joining(" "));
    }

    private void addMethodNode(DefaultMutableTreeNode parentNode, MethodDeclaration methodSignature) {

        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode( String.format("%s %s(%s):%s", getModifiers(methodSignature), methodSignature.getName(),
                getParameters(methodSignature), methodSignature.getType()));
        parentNode.add(fileNode);
    }

    private  String getParameters(MethodDeclaration methodSignature) {
        return methodSignature.getParameters().stream().map(Node::toString).collect(Collectors.joining(", "));
    }

    private String getModifiers(MethodDeclaration methodSignature) {
        return methodSignature.getModifiers().stream().map(modifier -> modifier.getKeyword().asString()).collect(Collectors.joining(" "));
    }


}
