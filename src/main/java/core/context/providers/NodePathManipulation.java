package core.context.providers;

import com.sun.source.tree.Tree;
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
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
        Point point = e.getLocationOnScreen();
        if (tree.getSelectionPaths()==null || tree.getSelectionPaths().length==1){
            tree.setSelectionPath(path);
            String[] paths = extractPaths(path);
            ArrayList<String[]> pathsList = new ArrayList<>();
            pathsList.add(paths);
            return new ProjectStructureSelectionContextDTO(new TreePath[]{path}, pathsList, point);
        }
        else{
            TreePath[] selectionPaths = tree.getSelectionPaths();
            List<String[]> nodeNamesList = new ArrayList<>();
            for (TreePath selectionPath : selectionPaths) {
                String[] paths = extractPaths(selectionPath);
                nodeNamesList.add(paths);
            }
            return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point);
        }


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
                DefaultMutableTreeNode addedNode = addNodeWithAllChildren(iteratedCreatedFile);
                model.insertNodeInto(addedNode, iteratedNode, iteratedNode.getChildCount());
            }
        }
    }

    private DefaultMutableTreeNode addNodeWithAllChildren(Path filePath) {
        File file = filePath.toFile();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(filePath.getFileName().toString());
        if (file.isDirectory()){
            extractNodes(file, node);
        }
        return node;
    }

    private void extractNodes(File directory, DefaultMutableTreeNode parent) {
        for (File childFile : directory.listFiles()) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childFile.getName());
            parent.add(childNode);
            if (childFile.isDirectory()){
                extractNodes(childFile, childNode);
            }
        }
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext(ActionEvent actionEvent){
        JTree tree = (JTree) actionEvent.getSource();
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null){
            return new ProjectStructureSelectionContextDTO(new TreePath[]{}, new ArrayList<>(), null);
        }
        List<String[]> nodeNamesList = new ArrayList<>();
        Point point = null;
        for (TreePath selectionPath : selectionPaths) {
            point = tree.getPathBounds(selectionPath).getLocation();
            String[] nodeNames = extractPaths(selectionPath);
            nodeNamesList.add(nodeNames);
        }
        return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

}
