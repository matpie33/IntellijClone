package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.context.contextMenu.ContextType;
import root.core.context.providers.ContextProvider;
import root.core.dto.*;
import root.core.fileio.FileIO;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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

    private List<String> directoriesThatShouldMergeNodes = Arrays.asList("java", "JDK");

    public ProjectStructureNodesHandler(ApplicatonState applicatonState, FileIO fileIO) {
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
    }

    public void renameNode (DefaultMutableTreeNode node, String newFileName){
        TreeNodeFileDTO userObject = (TreeNodeFileDTO) node.getUserObject();
        userObject.setDisplayName(newFileName);
    }

    public DefaultMutableTreeNode addNodesForSources(File root, boolean mergeNodes){
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  root.getName(), root.getAbsolutePath()));
        File[] children = root.listFiles();
        if (children.length==0){
            return top;
        }
        List<String> nodesToMerge = new ArrayList<>();
        if (directoriesThatShouldMergeNodes.contains(root.getName())){
            mergeNodes  = true;
        }
        if (children.length==1 && children[0].isDirectory()){
            nodesToMerge.add(root.getName());
            addNode(Optional.empty(), children[0], nodesToMerge, mergeNodes);
        }
        else{
            for (File file : children) {
                nodesToMerge.clear();
                addNode(Optional.of(top), file, nodesToMerge, mergeNodes);
            }
        }
        return top;

    }

    public DefaultMutableTreeNode addNodesForJDKSources(DefaultMutableTreeNode rootNode, File root){
        DefaultMutableTreeNode jdkNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.EMPTY, "JDK", "JDK"));
        File[] files = root.listFiles();
        if (files==null){
            throw new RuntimeException("JDK sources directory is empty");
        }
        for (File file : files) {
            DefaultMutableTreeNode node = addNodesForSources(file, true);
            jdkNode.add(node);
        }
        rootNode.add(jdkNode);
        return jdkNode;
    }

    private void addNode(Optional<DefaultMutableTreeNode> parentNode, File file, List<String> nodesToMerge, boolean mergeNodes) {
        if (directoriesThatShouldMergeNodes.contains(file.getName())){
            mergeNodes = true;
        }
        if (file.isDirectory()){
            File[] children = file.listFiles();
            nodesToMerge.add(file.getName());
            if (mergeNodes && children.length==1 && children[0].isDirectory()){
                addNode(parentNode, children[0], nodesToMerge, mergeNodes);
            }
            else {
                String mergedNodes = String.join(".", nodesToMerge);
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  mergedNodes, mergedNodes.replace(".", "/")));
                nodesToMerge.clear();
                parentNode.ifPresent(parent->parent.add(fileNode));
                for (File child : children) {
                    addNode(Optional.of(fileNode), child, nodesToMerge, mergeNodes);
                }
            }
        }
        else{
            assert(parentNode.isPresent());
            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.SOURCE_CLASS,  file.getName(), file.getName()));
            parentNode.get().add(fileNode);
        }
    }


    public void addExternalDependencies(DefaultTreeModel model, Map<String, List<File>> jarToClassesMap, DefaultMutableTreeNode root) {

        DefaultMutableTreeNode mavenNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY,  "maven", "maven"));
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
        TreeNodeFileDTO jarNodeDTO = new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY, pathFromRepoToJar, fullPathToJar);
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
                TreeNodeFileDTO treeNodeFileDTO = new TreeNodeFileDTO(node.getKey().contains(".class") ? TreeNodeFileDTO.Type.CLASS_FROM_JAR : TreeNodeFileDTO.Type.DIRECTORY, mergedValue, mergedValue.replace(".", "/"));
                DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(treeNodeFileDTO);
                model.insertNodeInto(currentNode, parentNode, parentNode.getChildCount());
                localParent = currentNode;
            }
            else{
                TreeNodeFileDTO treeNodeFileDTO = new TreeNodeFileDTO(node.getKey().contains(".class") ? TreeNodeFileDTO.Type.CLASS_FROM_JAR : TreeNodeFileDTO.Type.DIRECTORY, node.getKey(), node.getKey() );
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
        List<Path> modifiedFiles = fileSystemChangeDTO.getModifiedFiles();
        if (createdFiles.isEmpty() && deletedFiles.isEmpty() && modifiedFiles.isEmpty()){
            return;
        }

        for (Path createdFile : createdFiles) {
            findOrCreateNodesForPath(createdFile, rootNode, model);
        }
        for (Path modifiedFile : modifiedFiles) {
            findOrCreateNodesForPath(modifiedFile, rootNode, model);
        }
        for (Path deletedFile : deletedFiles) {
            deleteNode(deletedFile, rootNode, model);
        }

    }

    private void deleteNode(Path deletedFile, DefaultMutableTreeNode rootNode, DefaultTreeModel model) {
        Path projectPath = applicatonState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(deletedFile);
        DefaultMutableTreeNode parentNode = rootNode;
        DefaultMutableTreeNode node = null;
        for (Path pathPart : fileRelativeToProjectPath) {
            node = tryGetChildNodeByName(parentNode, pathPart.toString()).orElse(null);
            if (node == null) {
                break;
            }
            parentNode = node;
        }
        if (node!=null){
            model.removeNodeFromParent(node);
        }
    }


    private void findOrCreateNodesForPath (Path path, DefaultMutableTreeNode rootNode, DefaultTreeModel model){
        Path projectPath = applicatonState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(path);
        DefaultMutableTreeNode parentNode = rootNode;
        DefaultMutableTreeNode node = null;
        for (Iterator<Path> iterator = fileRelativeToProjectPath.iterator(); iterator.hasNext(); ) {
            Path pathPart = iterator.next();
            final DefaultMutableTreeNode localParent = parentNode;
            node = tryGetChildNodeByName(parentNode, pathPart.toString()).orElseGet(()->createNode(iterator.hasNext(), pathPart.toString(), model, localParent));
            parentNode = node;
        }
        File file = path.toFile();
        if (file.isDirectory() && node != null){
            for (File child : file.listFiles()) {
                extractNodes(child, node);
            }
        }
    }

    private DefaultMutableTreeNode createNode(boolean isLast, String name, DefaultTreeModel model, DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNodeFileDTO(isLast ? TreeNodeFileDTO.Type.SOURCE_CLASS : TreeNodeFileDTO.Type.DIRECTORY, name, ""));
        model.insertNodeInto(node, parentNode, parentNode.getChildCount());
        return node;
    }

    private Optional<DefaultMutableTreeNode> tryGetChildNodeByName(DefaultMutableTreeNode parentNode, String searchedName) {
        Optional<DefaultMutableTreeNode> foundChild = Optional.empty();
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            TreeNodeFileDTO userObject = (TreeNodeFileDTO) child.getUserObject();
            String nodeName = userObject.getDisplayName();
            if (nodeName.equals(searchedName)) {
                foundChild = Optional.of(child);
                break;
            }
        }
        return foundChild;
    }

    private void extractNodes(File directory, DefaultMutableTreeNode parent) {
        boolean isDirectory = directory.isDirectory();
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new TreeNodeFileDTO(isDirectory ? TreeNodeFileDTO.Type.DIRECTORY: TreeNodeFileDTO.Type.SOURCE_CLASS,  directory.getName(), directory.getName()));
        if (!parentContainsNode(parent, childNode)){
            parent.add(childNode);
        }
        if(isDirectory){
            for (File file : directory.listFiles()) {
                extractNodes(file, childNode);
            }
        }
    }

    private boolean parentContainsNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode childNode) {
        for (int i=0; i<parent.getChildCount(); i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (node.getUserObject().equals(childNode.getUserObject())){
                return true;
            }
        }
        return false;
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
        return new DefaultMutableTreeNode(new TreeNodeFileDTO(TreeNodeFileDTO.Type.EMPTY, "No projects loaded", ""));
    }
}