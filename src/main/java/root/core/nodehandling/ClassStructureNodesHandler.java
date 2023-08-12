package root.core.nodehandling;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Component;
import root.core.dto.ClassStructureTreeElementDTO;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.stream.Collectors;

@Component
public class ClassStructureNodesHandler {

    public Position getPositionInTextEditorForClassNode (DefaultMutableTreeNode defaultMutableTreeNode){
        return ((ClassStructureTreeElementDTO) defaultMutableTreeNode.getUserObject()).getStartingPosition();
    }

    public DefaultMutableTreeNode createEmptyRootNode (){
        return new DefaultMutableTreeNode(new ClassStructureTreeElementDTO());
    }

    public DefaultMutableTreeNode build (TypeDeclaration<?> classOrInterfaceDeclaration){
        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        Position position = classOrInterfaceDeclaration.getRange().orElseThrow().begin;
        classStructureTreeElementDTO.setStartingPosition(position);
        classStructureTreeElementDTO.setDisplayName(classOrInterfaceDeclaration.getNameAsString());
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(classStructureTreeElementDTO);
        for (FieldDeclaration fieldDeclaration : classOrInterfaceDeclaration.getFields()) {
            addFieldNode(top, fieldDeclaration);
        }
        for (MethodDeclaration methodDeclaration : classOrInterfaceDeclaration.getMethods()) {
            addMethodNode(top, methodDeclaration);
        }
        return top;

    }

    private void addFieldNode(DefaultMutableTreeNode parentNode, FieldDeclaration fieldDeclaration) {

        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        classStructureTreeElementDTO.setStartingPosition(fieldDeclaration.getVariable(0).getName().getRange().orElseThrow().begin);
        String displayName = String.format("%s %s:%s", getModifiers(fieldDeclaration),
                getFieldNames(fieldDeclaration), getType(fieldDeclaration));
        classStructureTreeElementDTO.setDisplayName(displayName);
        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(classStructureTreeElementDTO);
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

    private void addMethodNode(DefaultMutableTreeNode parentNode, MethodDeclaration methodDeclaration) {

        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        String displayName = String.format("%s %s(%s):%s", getModifiers(methodDeclaration), methodDeclaration.getName(),
                getParameters(methodDeclaration), methodDeclaration.getType());
        classStructureTreeElementDTO.setDisplayName(displayName);
        classStructureTreeElementDTO.setStartingPosition(methodDeclaration.getName().getRange().orElseThrow().begin);
        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(classStructureTreeElementDTO);
        parentNode.add(methodNode);
    }

    private  String getParameters(MethodDeclaration methodSignature) {
        return methodSignature.getParameters().stream().map(Node::toString).collect(Collectors.joining(", "));
    }

    private String getModifiers(MethodDeclaration methodSignature) {
        return methodSignature.getModifiers().stream().map(modifier -> modifier.getKeyword().asString()).collect(Collectors.joining(" "));
    }


}
