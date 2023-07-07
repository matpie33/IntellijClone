package core.context.providers;

import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileSystemChangeDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Component
public class NodePathManipulation implements ContextProvider<ProjectStructureSelectionContextDTO> {

    private ApplicatonState applicatonState;

    public NodePathManipulation(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext (MouseEvent e){

        JTree tree = (JTree) e.getSource();
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        String[] paths = extractPaths(path);
        return new ProjectStructureSelectionContextDTO(path, paths);

    }

    private String[] extractPaths(TreePath path) {
        String [] paths = new String [path.getPathCount()];
        for (int i=0; i<paths.length; i++){
            DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
            paths[i] =(String) pathComponent.getUserObject();
        }
        return paths;
    }

    public void updateTreeStructure(FileSystemChangeDTO fileSystemChangeDTO, DefaultMutableTreeNode rootNode,
                                    DefaultTreeModel model){
        List<Path> createdFiles = fileSystemChangeDTO.getCreatedFiles();
        List<Path> deletedFiles = fileSystemChangeDTO.getDeletedFiles();

        Enumeration<TreeNode> enumeration = rootNode.depthFirstEnumeration();
        List<DefaultMutableTreeNode> nodesToDelete = new ArrayList<>();
        while (enumeration.hasMoreElements()){
            DefaultMutableTreeNode iteratedNode = (DefaultMutableTreeNode) enumeration.nextElement();
            Path iteratedNodeFilePath = getFilePathFromNode(iteratedNode);
            handleAddNode(model, createdFiles, iteratedNode, iteratedNodeFilePath);
            handleRemoveNode(deletedFiles, nodesToDelete, iteratedNode, iteratedNodeFilePath);
        }
        nodesToDelete.forEach(model::removeNodeFromParent);
    }

    private Path getFilePathFromNode(DefaultMutableTreeNode iteratedNode) {
        Object[] nodesPath  = iteratedNode.getUserObjectPath();
        String[] nodesPathStrings = Arrays.copyOf(nodesPath, nodesPath.length, String[].class);
        return Path.of(applicatonState.getProjectPath(), nodesPathStrings);
    }

    private void handleRemoveNode(List<Path> deletedFiles, List<DefaultMutableTreeNode> nodesToDelete, DefaultMutableTreeNode iteratedNode, Path iteratedNodeFilePath) {
        if (deletedFiles.contains(iteratedNodeFilePath)){
            deletedFiles.remove(iteratedNodeFilePath);
            nodesToDelete.add(iteratedNode);
        }
    }

    private void handleAddNode(DefaultTreeModel model, List<Path> createdFiles, DefaultMutableTreeNode iteratedNode, Path iteratedNodeFilePath) {
        for (int i = 0; i < createdFiles.size(); i++) {
            Path iteratedCreatedFile = createdFiles.get(i);

            if (iteratedCreatedFile.getParent().equals(iteratedNodeFilePath)) {
                createdFiles.remove(iteratedCreatedFile);
                i--;
                DefaultMutableTreeNode addedNode = new DefaultMutableTreeNode(iteratedCreatedFile.getFileName().toString());
                model.insertNodeInto(addedNode, iteratedNode, iteratedNode.getChildCount());
            }
        }
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext(ActionEvent actionEvent){
        JTree tree = (JTree) actionEvent.getSource();
        TreePath selectionPath = tree.getSelectionPath();
        String[] nodeNames;
        if (selectionPath != null){
            nodeNames = extractPaths(selectionPath);
        }
        else{
            nodeNames = new String [] {};
        }
        return new ProjectStructureSelectionContextDTO(selectionPath, nodeNames);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

}
