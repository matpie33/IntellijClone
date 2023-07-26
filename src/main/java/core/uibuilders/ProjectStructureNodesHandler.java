package core.uibuilders;

import core.backend.FileIO;
import core.context.providers.ContextProvider;
import core.contextMenu.ContextType;
import core.dto.*;
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
import java.util.List;
import java.util.*;

@Component
public class ProjectStructureNodesHandler implements ContextProvider<ProjectStructureSelectionContextDTO> {

    private ApplicatonState applicatonState;

    private FileIO fileIO;

    public ProjectStructureNodesHandler(ApplicatonState applicatonState, FileIO fileIO) {
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
    }

    public void renameNode (DefaultMutableTreeNode node, String newFileName){
        TreeNodeFileDTO userObject = (TreeNodeFileDTO) node.getUserObject();
        userObject.setDisplayName(newFileName);
    }

    public DefaultMutableTreeNode build (File root, List<FileDTO> children){
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  root.getName()));
        for (FileDTO child : children) {
            addNode(top, child);
        }
        return top;

    }

    private void addNode(DefaultMutableTreeNode parentNode, FileDTO child) {
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  child.getName()));
        parentNode.add(fileNode);
        if (child instanceof DirectoryDTO){
            extractChildren(fileNode, (DirectoryDTO)child);
        }
    }

    private void extractChildren(DefaultMutableTreeNode parentNode, DirectoryDTO directoryDTO) {
        for (FileDTO child : directoryDTO.getFiles()) {
            addNode(parentNode, child);
        }
    }


    public void addExternalDependencies(DefaultTreeModel model, Map<String, List<File>> jarToClassesMap, DefaultMutableTreeNode root) {

        DefaultMutableTreeNode mavenNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  "maven"));
        model.insertNodeInto(mavenNode, root, root.getChildCount());
        for (Map.Entry<String, List<File>> jarToClassesEntry : jarToClassesMap.entrySet()) {
            String fullPathToJar = jarToClassesEntry.getKey();
            DefaultMutableTreeNode jarNode = createJarNode(fullPathToJar);
            model.insertNodeInto(jarNode, mavenNode, mavenNode.getChildCount());

            List<File> classes = jarToClassesEntry.getValue();
            JarPathNode jarRootNode = new JarPathNode();

            createNodesHierarchyForClasses(fullPathToJar, classes, jarRootNode);
            List<String> nodesToMerge = new ArrayList<>();
            createTreeNodesFromDTOs(model, jarRootNode, nodesToMerge, jarNode);


        }
    }

    private void createNodesHierarchyForClasses(String fullPathToJar, List<File> classes, JarPathNode jarRootNode) {
        for (File classFile : classes) {
            JarPathNode parent = jarRootNode;
            String pathToClass = classFile.toString();
            String[] directories = pathToClass.split("\\\\");
            for (String directory : directories) {
                JarPathNode subNode = parent.getNode(directory);
                if (subNode == null) {
                    subNode = new JarPathNode();
                    subNode.setPathTojar(fullPathToJar);
                    parent.addNode(directory, subNode);
                }
                parent = subNode;

            }

        }
    }

    private DefaultMutableTreeNode createJarNode(String fullPathToJar) {
        String pathFromRepoToJar = fullPathToJar.replace(applicatonState.getLocalRepositoryPath(), "").replace("\\", ".").replaceFirst(".", "");
        TreeNodeFileDTO jarNodeDTO = new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY, pathFromRepoToJar);
        return new DefaultMutableTreeNode(jarNodeDTO);
    }

    private void createTreeNodesFromDTOs(DefaultTreeModel model, JarPathNode parentJarPathNode, List<String> nodesToMerge, DefaultMutableTreeNode parentNode) {

        for (Map.Entry<String, JarPathNode> node : parentJarPathNode.getNodes()) {

            DefaultMutableTreeNode localParent = parentNode;
            Set<Map.Entry<String, JarPathNode>> childNodes = node.getValue().getNodes();
            if (childNodes.size() ==1){
                nodesToMerge.add(node.getKey());
            }
            else if (!nodesToMerge.isEmpty()){
                nodesToMerge.add(node.getKey());
                String mergedValue = String.join(".", nodesToMerge);
                nodesToMerge.clear();
                TreeNodeFileDTO treeNodeFileDTO = new TreeNodeFileDTO(node.getKey().contains(".class") ? TreeNodeFileDTO.Type.CLASS_FROM_JAR : TreeNodeFileDTO.Type.DIRECTORY, mergedValue);
                DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(treeNodeFileDTO);
                model.insertNodeInto(currentNode, parentNode, parentNode.getChildCount());
                localParent = currentNode;
            }
            else{
                TreeNodeFileDTO treeNodeFileDTO = new TreeNodeFileDTO(node.getKey().contains(".class") ? TreeNodeFileDTO.Type.CLASS_FROM_JAR : TreeNodeFileDTO.Type.DIRECTORY, node.getValue().getPathTojar(), node.getKey());
                DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(treeNodeFileDTO);
                model.insertNodeInto(currentNode, parentNode, parentNode.getChildCount());
                if (!childNodes.isEmpty()){
                    localParent = currentNode;
                }
            }
            createTreeNodesFromDTOs(model, node.getValue(), nodesToMerge, localParent);
        }
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext (MouseEvent e){

        JTree tree = (JTree) e.getSource();
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        Point point = e.getLocationOnScreen();
        if (tree.getSelectionPaths()==null || tree.getSelectionPaths().length==1){
            tree.setSelectionPath(path);
            TreeNodeFileDTO[] paths = extractPaths(path);
            List<TreeNodeFileDTO[]> pathsList = new ArrayList<>();
            pathsList.add(paths);
            File file = fileIO.getFile(paths); //TODO move it outside this class
            return new ProjectStructureSelectionContextDTO(new TreePath[]{path}, pathsList, point, file);
        }
        else{
            TreePath[] selectionPaths = tree.getSelectionPaths();
            List<TreeNodeFileDTO[]> nodeNamesList = new ArrayList<>();
            for (TreePath selectionPath : selectionPaths) {
                TreeNodeFileDTO[] paths = extractPaths(selectionPath);
                nodeNamesList.add(paths);
            }
            return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);
        }


    }

    private TreeNodeFileDTO[] extractPaths(TreePath path) {
        TreeNodeFileDTO [] paths = new TreeNodeFileDTO [path.getPathCount()];
        for (int i=0; i<paths.length; i++){
            DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
            paths[i] =(TreeNodeFileDTO) pathComponent.getUserObject();
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
        String[] nodesPath  = Arrays.stream(iteratedNode.getUserObjectPath()).map(TreeNodeFileDTO.class::cast).map(TreeNodeFileDTO::getDisplayName).toArray(String[]::new);
        return Path.of(applicatonState.getProjectPath().getParent(), nodesPath);
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
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  filePath.getFileName().toString()));
        if (file.isDirectory()){
            extractNodes(file, node);
        }
        return node;
    }

    private void extractNodes(File directory, DefaultMutableTreeNode parent) {
        for (File childFile : directory.listFiles()) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  childFile.getName()));
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
            return new ProjectStructureSelectionContextDTO(new TreePath[]{}, new ArrayList<>(), null, null);
        }
        List<TreeNodeFileDTO[]> nodeNamesList = new ArrayList<>();
        Point point = null;
        for (TreePath selectionPath : selectionPaths) {
            point = tree.getPathBounds(selectionPath).getLocation();
            TreeNodeFileDTO[] nodeNames = extractPaths(selectionPath);
            nodeNamesList.add(nodeNames);
        }
        return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

    public String getText(DefaultMutableTreeNode node) {
        return ((TreeNodeFileDTO) node.getUserObject()).getDisplayName();
    }

    public DefaultMutableTreeNode createEmptyRootNode() {
        return new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.EMPTY, "No projects loaded"));
    }
}