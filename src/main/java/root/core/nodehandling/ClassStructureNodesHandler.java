package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.dto.*;

import javax.swing.tree.DefaultMutableTreeNode;

@Component
public class ClassStructureNodesHandler {

    public TokenPositionDTO getPositionInTextEditorForClassNode (DefaultMutableTreeNode defaultMutableTreeNode){
        ClassStructureTreeElementDTO treeElement = (ClassStructureTreeElementDTO) defaultMutableTreeNode.getUserObject();
        int startingPosition = treeElement.getStartingPosition();
        String displayName = treeElement.getDisplayName();
        return new TokenPositionDTO(startingPosition, displayName.length());
    }

    public DefaultMutableTreeNode createEmptyRootNode (){
        return new DefaultMutableTreeNode(new ClassStructureTreeElementDTO());
    }

    public DefaultMutableTreeNode build (ClassStructureDTO classStructureDTO){
        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        ClassDeclarationDTO classDeclaration = classStructureDTO.getClassDeclaration();
        classStructureTreeElementDTO.setStartingPosition(classDeclaration.getStartingOffset());
        classStructureTreeElementDTO.setDisplayName(classDeclaration.getName());
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(classStructureTreeElementDTO);
        for (FieldDeclarationDTO fieldDeclarationDTO : classStructureDTO.getFieldDeclarations()) {
            addFieldNode(top, fieldDeclarationDTO);
        }
        for (MethodDeclarationDTO methodDeclaration : classStructureDTO.getMethodsDeclarations()) {
            addMethodNode(top, methodDeclaration);
        }
        return top;

    }

    private void addFieldNode(DefaultMutableTreeNode parentNode, FieldDeclarationDTO fieldDeclarationDTO) {

        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        classStructureTreeElementDTO.setStartingPosition(fieldDeclarationDTO.getStartOffset());
        String displayName = String.format("%s %s:%s", String.join(" ", fieldDeclarationDTO.getModifiers()),
                String.join(",", fieldDeclarationDTO.getName()), fieldDeclarationDTO.getType());
        classStructureTreeElementDTO.setDisplayName(displayName);
        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(classStructureTreeElementDTO);
        parentNode.add(fieldNode);
    }

    private void addMethodNode(DefaultMutableTreeNode parentNode, MethodDeclarationDTO methodDeclarationDTO) {

        ClassStructureTreeElementDTO classStructureTreeElementDTO = new ClassStructureTreeElementDTO();
        String displayName = String.format("%s %s(%s):%s", methodDeclarationDTO.getModifiers(), methodDeclarationDTO.getName(),
                String.join(",", methodDeclarationDTO.getParameters()), methodDeclarationDTO.getType());
        classStructureTreeElementDTO.setDisplayName(displayName);
        classStructureTreeElementDTO.setStartingPosition(methodDeclarationDTO.getStartOffset());
        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(classStructureTreeElementDTO);
        parentNode.add(methodNode);
    }

}
