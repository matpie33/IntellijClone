package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.context.contextMenu.ContextType;
import root.core.context.providers.ContextProvider;
import root.core.dto.ApplicationState;
import root.core.dto.FileSystemChangeDTO;
import root.core.dto.JarPathNode;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.fileio.FileIO;
import root.core.ui.tree.ProjectStructureNode;
import root.core.ui.tree.ProjectStructureNodeType;

import javax.swing.*;
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

    private ApplicationState applicationState;

    private FileIO fileIO;

    private List<String> directoriesThatShouldMergeNodes = Arrays.asList("java", "JDK");

    public ProjectStructureNodesHandler(ApplicationState applicationState, FileIO fileIO) {
        this.applicationState = applicationState;
        this.fileIO = fileIO;
    }

    public void renameNode (ProjectStructureNode node, String newFileName){
        node.setDisplayName(newFileName);
        node.setFilePath(newFileName);
    }

    public ProjectStructureNode addNodesForSources(File rootDirectory, ClassOrigin classOrigin, boolean isProjectRoot){
        ProjectStructureNode top = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.DIRECTORY, rootDirectory.getName(), isProjectRoot? "": rootDirectory.getName(), false);
        File[] subdirectories = rootDirectory.listFiles();
        if (subdirectories.length==0){
            return top;
        }
        boolean isInsideJavaSources = false;
        List<String> nodesToMerge = new ArrayList<>();
        if (directoriesThatShouldMergeNodes.contains(rootDirectory.getName())){
            isInsideJavaSources  = true;
        }
        if (subdirectories.length==1 && subdirectories[0].isDirectory()){
            nodesToMerge.add(rootDirectory.getName());
            addNode(Optional.empty(), subdirectories[0], nodesToMerge, isInsideJavaSources);
        }
        else{
            for (File file : subdirectories) {
                nodesToMerge.clear();
                addNode(Optional.of(top), file, nodesToMerge, isInsideJavaSources);
            }
        }
        return top;

    }

    public void addNodesForJDKSources(ProjectStructureNode rootNode, File rootFile){
        ClassOrigin classOrigin = ClassOrigin.JDK;
        ProjectStructureNode jdkNode = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.EMPTY, "JDK", "", false);
        File[] files = rootFile.listFiles();
        if (files==null){
            throw new RuntimeException("JDK sources directory is empty");
        }
        for (File file : files) {
            ProjectStructureNode node = addNodesForSources(file,classOrigin, false);
            jdkNode.add(node);
        }
        rootNode.add(jdkNode);

    }

    private void addNode(Optional<ProjectStructureNode> parentNode, File file, List<String> nodesToMerge, boolean isInsideJavaSources) {
        if (directoriesThatShouldMergeNodes.contains(file.getName())){
            isInsideJavaSources = true;
        }
        if (file.isDirectory()){
            File[] children = file.listFiles();
            nodesToMerge.add(file.getName());
            if (isInsideJavaSources && children.length==1 && children[0].isDirectory()){
                addNode(parentNode, children[0], nodesToMerge, true);
            }
            else {
                String displayName = String.join(".", nodesToMerge);
                String subPath = String.join("/", nodesToMerge);
                ProjectStructureNode fileNode = new ProjectStructureNode(ClassOrigin.SOURCES, ProjectStructureNodeType.DIRECTORY, displayName, subPath, isInsideJavaSources);
                nodesToMerge.clear();
                parentNode.ifPresent(parent->parent.add(fileNode));
                for (File child : children) {
                    addNode(Optional.of(fileNode), child, nodesToMerge, isInsideJavaSources);
                }
            }
        }
        else{
            assert(parentNode.isPresent());
            ProjectStructureNode fileNode = new ProjectStructureNode(ClassOrigin.JDK, ProjectStructureNodeType.FILE, file.getName(), file.getName(), isInsideJavaSources);
            parentNode.get().add(fileNode);
        }
    }


    public void addExternalDependencies(DefaultTreeModel model, Map<String, List<File>> jarToClassesMap, ProjectStructureNode rootNode) {

        ClassOrigin classOrigin = ClassOrigin.MAVEN;
        ProjectStructureNode mavenNode = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.EMPTY,  "maven", "", true);
        model.insertNodeInto(mavenNode, rootNode, rootNode.getChildCount());
        for (Map.Entry<String, List<File>> jarToClassesEntry : jarToClassesMap.entrySet()) {
            String fullPathToJar = jarToClassesEntry.getKey();
            ProjectStructureNode jarNode = createJarNode(fullPathToJar, classOrigin);
            model.insertNodeInto(jarNode, mavenNode, mavenNode.getChildCount());

            List<File> classes = jarToClassesEntry.getValue();
            JarPathNode jarRootNode = new JarPathNode();

            createNodesHierarchyForClasses(fullPathToJar, classes, jarRootNode);
            List<String> nodesToMerge = new ArrayList<>();
            createTreeNodesFromDTOs(model, jarRootNode, nodesToMerge, jarNode, classOrigin);


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
                    subNode.setPathToJar(fullPathToJar);
                    parent.addNode(directory, subNode);
                }
                parent = subNode;

            }

        }
    }

    private ProjectStructureNode createJarNode(String fullPathToJar, ClassOrigin classOrigin) {
        String pathFromRepoToJar = fullPathToJar.replace(applicationState.getLocalRepositoryPath(), "").replace("\\", ".").replaceFirst(".", "");
        return new ProjectStructureNode(classOrigin, ProjectStructureNodeType.EMPTY, pathFromRepoToJar, fullPathToJar, false);
    }

    private void createTreeNodesFromDTOs(DefaultTreeModel model, JarPathNode parentJarPathNode, List<String> nodesToMerge, ProjectStructureNode parentNode, ClassOrigin classOrigin) {

        for (Map.Entry<String, JarPathNode> node : parentJarPathNode.getNodes()) {

            ProjectStructureNode localParent = parentNode;
            Set<Map.Entry<String, JarPathNode>> childNodes = node.getValue().getNodes();
            boolean isDirectory = !childNodes.isEmpty();
            if (childNodes.size() ==1){
                nodesToMerge.add(node.getKey());
            }
            else if (!nodesToMerge.isEmpty()){
                nodesToMerge.add(node.getKey());
                String displayValue = String.join(".", nodesToMerge);
                String pathValue = String.join("/", nodesToMerge);
                nodesToMerge.clear();
                ProjectStructureNode currentNode = createProjectStructureNode(classOrigin, isDirectory, displayValue, pathValue, true);
                model.insertNodeInto(currentNode, parentNode, parentNode.getChildCount());
                localParent = currentNode;
            }
            else{
                ProjectStructureNode currentNode = createProjectStructureNode(classOrigin, isDirectory, node.getKey(), node.getKey(), true);
                model.insertNodeInto(currentNode, parentNode, parentNode.getChildCount());
                if (!childNodes.isEmpty()){
                    localParent = currentNode;
                }
            }
            createTreeNodesFromDTOs(model, node.getValue(), nodesToMerge, localParent, classOrigin);
        }
    }

    private ProjectStructureNode createProjectStructureNode(ClassOrigin classOrigin, boolean isDirectory, String displayValue, String pathValue, boolean isInsideJavaSources) {
        return new ProjectStructureNode(classOrigin, isDirectory ? ProjectStructureNodeType.DIRECTORY : ProjectStructureNodeType.FILE, displayValue, pathValue, isInsideJavaSources);
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext (MouseEvent e){

        JTree tree = (JTree) e.getSource();
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        Point point = e.getLocationOnScreen();
        if (tree.getSelectionPaths()==null || tree.getSelectionPaths().length==1){
            tree.setSelectionPath(path);
            ProjectStructureNode[] paths = extractNodes(path);
            List<ProjectStructureNode[]> pathsList = new ArrayList<>();
            pathsList.add(paths);
            File file = fileIO.getFile(paths); //TODO move it outside this class
            return new ProjectStructureSelectionContextDTO(new TreePath[]{path}, pathsList, point, file);
        }
        else{
            TreePath[] selectionPaths = tree.getSelectionPaths();
            List<ProjectStructureNode[]> nodeNamesList = new ArrayList<>();
            for (TreePath selectionPath : selectionPaths) {
                ProjectStructureNode[] paths = extractNodes(selectionPath);
                nodeNamesList.add(paths);
            }
            return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);
        }


    }

    private ProjectStructureNode[] extractNodes(TreePath path) {
        ProjectStructureNode[] paths = new ProjectStructureNode[path.getPathCount()];
        for (int i=0; i<paths.length; i++){
            ProjectStructureNode pathComponent = (ProjectStructureNode) path.getPathComponent(i);
            paths[i] = pathComponent;
        }
        return paths;
    }

    public void updateTreeStructure(FileSystemChangeDTO fileSystemChangeDTO, ProjectStructureNode rootNode,
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

    private void deleteNode(Path deletedFile, ProjectStructureNode rootNode, DefaultTreeModel model) {
        Path projectPath = applicationState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(deletedFile);
        boolean isDirectory = deletedFile.toFile().isDirectory();
        ProjectStructureNode node = getNodesForPathAndCreateOptionally(rootNode, fileRelativeToProjectPath, model, isDirectory, false);
        if (node!=null){
            model.removeNodeFromParent(node);
        }
    }


    private void findOrCreateNodesForPath (Path path, ProjectStructureNode rootNode, DefaultTreeModel model){
        Path projectPath = applicationState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(path);
        boolean isDirectory = path.toFile().isDirectory();
        ProjectStructureNode lastNode = getNodesForPathAndCreateOptionally(rootNode, fileRelativeToProjectPath, model, isDirectory, true);
        File file = path.toFile();
        if (file.isDirectory() && lastNode != null){
            for (File child : file.listFiles()) {
                extractNodes(child, lastNode, model, lastNode);
            }
        }
    }

    private ProjectStructureNode createNode(boolean isDirectory, String name, DefaultTreeModel model, ProjectStructureNode parentNode) {
        ProjectStructureNode node = createProjectStructureNode(parentNode.getClassOrigin(), isDirectory, name, name, parentNode.isInsideJavaSources());
        model.insertNodeInto(node, parentNode, parentNode.getChildCount());
        return node;
    }

    private ProjectStructureNode getNodesForPathAndCreateOptionally(ProjectStructureNode parentNode, Path filePath, DefaultTreeModel model, boolean isDirectory, boolean createIfNotExist) {

        Iterator<Path> iterator = filePath.iterator();
        ProjectStructureNode foundChild = null;
        if (!iterator.hasNext()){
            return null;
        }
        String pathPartString = iterator.next().toString();
        boolean isFoundChild = false;
        while (!pathPartString.isEmpty() && (iterator.hasNext() || isFoundChild)){
            isFoundChild = false;
            for (int i = 0; i < parentNode.getChildCount(); i++) {
                ProjectStructureNode child = (ProjectStructureNode) parentNode.getChildAt(i);
                String nodeName = child.getDisplayName();
                if (nodeName.equals(pathPartString)) {
                    parentNode = child;
                    isFoundChild = true;
                    pathPartString = iterator.hasNext()? iterator.next().toString() : "";
                    break;
                }

                Path path = Path.of(child.getFilePath());
                boolean mergedNodesContainPath = pathContainsString(pathPartString, path);
                if (mergedNodesContainPath) {
                    while (mergedNodesContainPath && iterator.hasNext()){
                        pathPartString = iterator.next().toString();
                        mergedNodesContainPath = pathContainsString(pathPartString, path);
                    }
                    if (mergedNodesContainPath){
                        pathPartString = "";
                    }
                    parentNode = child;
                    isFoundChild = true;
                    break;
                }

            }
            if (!isFoundChild && createIfNotExist){
                foundChild = createNode(isDirectory, pathPartString, model, parentNode);
            }
        }

        return foundChild;
    }

    private boolean pathContainsString(String pathPartString, Path path) {
        boolean mergedNodesContainPath = false;
        for (Path subpath : path) {
            if (subpath.toString().equals(pathPartString)){
                mergedNodesContainPath = true;
            }
        }
        return mergedNodesContainPath;
    }

    private void extractNodes(File directory, ProjectStructureNode parent, DefaultTreeModel model, ProjectStructureNode lastNode) {
        boolean isDirectory = directory.isDirectory();
        ProjectStructureNode childNode = createProjectStructureNode(lastNode.getClassOrigin(), isDirectory, directory.getName(), directory.getName(), lastNode.isInsideJavaSources());
        if (!parentContainsNode(parent, childNode)){
            model.insertNodeInto(childNode, parent, parent.getChildCount());
        }
        if(isDirectory){
            for (File file : directory.listFiles()) {
                extractNodes(file, childNode, model, lastNode);
            }
        }
    }

    private boolean parentContainsNode(ProjectStructureNode parent, ProjectStructureNode childNode) {
        for (int i=0; i<parent.getChildCount(); i++){
            ProjectStructureNode node = (ProjectStructureNode) parent.getChildAt(i);
            if (node.equals(childNode)){
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
        List<ProjectStructureNode[]> nodeNamesList = new ArrayList<>();
        Point point = null;
        for (TreePath selectionPath : selectionPaths) {
            point = tree.getPathBounds(selectionPath).getLocation();
            ProjectStructureNode[] nodeNames = extractNodes(selectionPath);
            nodeNamesList.add(nodeNames);
        }
        return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

    public String getText(ProjectStructureNode node) {
        return node.getDisplayName();
    }

    public ProjectStructureNode createEmptyRootNode() {
        return new ProjectStructureNode(ClassOrigin.SOURCES, ProjectStructureNodeType.EMPTY, "No projects loaded", "", false);
    }
}
