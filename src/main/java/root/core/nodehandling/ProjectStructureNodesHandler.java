package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.context.contextMenu.ContextType;
import root.core.context.providers.ContextProvider;
import root.core.dto.ApplicationState;
import root.core.dto.FileDTO;
import root.core.dto.FileSystemChangeDTO;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.fileio.FileIO;
import root.core.ui.tree.ProjectStructureModel;
import root.core.ui.tree.ProjectStructureNode;
import root.core.ui.tree.ProjectStructureNodeType;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class ProjectStructureNodesHandler implements ContextProvider<ProjectStructureSelectionContextDTO> {

    private ApplicationState applicationState;

    private FileIO fileIO;

    public ProjectStructureNodesHandler(ApplicationState applicationState, FileIO fileIO) {
        this.applicationState = applicationState;
        this.fileIO = fileIO;
    }

    public void renameNode (ProjectStructureNode node, String newFileName){
        node.setDisplayName(newFileName);
        node.setFilePath(newFileName);
    }

    public ProjectStructureNode addNodesForSources(ProjectStructureModel model, File rootDirectory, ClassOrigin classOrigin, boolean isProjectRoot){
        ProjectStructureNode topNode = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.DIRECTORY, rootDirectory.getName(), isProjectRoot? "": rootDirectory.getName(), false);
        File[] subdirectories = rootDirectory.listFiles();
        if (subdirectories.length==0){
            return topNode;
        }
        createNodesForSubdirectories(rootDirectory,model, topNode, subdirectories);
        return topNode;

    }

    private void createNodesForSubdirectories(File rootDirectory, ProjectStructureModel model, ProjectStructureNode parent, File[] subdirectories) {
        for (File file : subdirectories) {
            if (file.isDirectory()){
                Path rootDirectoryPath = rootDirectory.toPath();
                Path path = file.toPath();
                Path fileRelativeToRootDirectory = rootDirectoryPath.relativize(path);
                ProjectStructureNode localParent = model.addChildWithPath(parent, fileRelativeToRootDirectory.toString(), false);
                File[] list = file.listFiles();
                createNodesForSubdirectories(rootDirectory, model, localParent, list);
            }
            else{
                Path rootDirectoryPath = rootDirectory.toPath();
                Path path = file.toPath();
                Path fileRelativeToRootDirectory = rootDirectoryPath.relativize(path);
                model.addChildWithPath(parent, fileRelativeToRootDirectory.toString(), true);
            }
        }
    }

    public void addNodesForJDKSources(ProjectStructureModel model, ProjectStructureNode rootNode, File rootFile){
        ClassOrigin classOrigin = ClassOrigin.JDK;
        ProjectStructureNode jdkNode = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.JDK_ROOT, "JDK", "", false);
        File[] files = rootFile.listFiles();
        if (files==null){
            throw new RuntimeException("JDK sources directory is empty");
        }
        for (File file : files) {
            ProjectStructureNode node = addNodesForSources(model, file,classOrigin, false);
            model.insertNodeInto(node, jdkNode, jdkNode.getChildCount());
        }
        model.insertNodeInto(jdkNode, rootNode, rootNode.getChildCount());

    }

    public void addExternalDependencies(ProjectStructureModel model, Map<String, List<FileDTO>> jarToClassesMap, ProjectStructureNode rootNode) {

        ClassOrigin classOrigin = ClassOrigin.MAVEN;
        ProjectStructureNode mavenNode = new ProjectStructureNode(classOrigin, ProjectStructureNodeType.MAVEN_ROOT,  "maven", "", false);
        model.insertNodeInto(mavenNode, rootNode, rootNode.getChildCount());
        for (Map.Entry<String, List<FileDTO>> jarToClassesEntry : jarToClassesMap.entrySet()) {
            String fullPathToJar = jarToClassesEntry.getKey();
            ProjectStructureNode jarNode = createJarNode(fullPathToJar, classOrigin);
            model.insertNodeInto(jarNode, mavenNode, mavenNode.getChildCount());

            List<FileDTO> files = jarToClassesEntry.getValue();
            for (FileDTO aFile : files) {
                Path path = aFile.getPath();
                model.getOrCreateChildWithPath(jarNode, path.toString(), !aFile.isDirectory());
            }


        }
    }

    private ProjectStructureNode createJarNode(String fullPathToJar, ClassOrigin classOrigin) {
        String pathFromRepoToJar = fullPathToJar.replace(applicationState.getLocalRepositoryPath(), "").replace("\\", ".").replaceFirst(".", "");
        return new ProjectStructureNode(classOrigin, ProjectStructureNodeType.JAR, pathFromRepoToJar, fullPathToJar, false);
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
            ProjectStructureNode[] paths = getNodes(path);
            List<ProjectStructureNode[]> pathsList = new ArrayList<>();
            pathsList.add(paths);
            File file = fileIO.getFile(paths); //TODO move it outside this class
            return new ProjectStructureSelectionContextDTO(new TreePath[]{path}, pathsList, point, file);
        }
        else{
            TreePath[] selectionPaths = tree.getSelectionPaths();
            List<ProjectStructureNode[]> nodeNamesList = new ArrayList<>();
            for (TreePath selectionPath : selectionPaths) {
                ProjectStructureNode[] paths = getNodes(selectionPath);
                nodeNamesList.add(paths);
            }
            return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);
        }


    }

    private ProjectStructureNode[] getNodes(TreePath path) {
        int pathCount = path.getPathCount();
        List<ProjectStructureNode> paths = new ArrayList<>();
        for (int i=0; i< pathCount; i++){
            ProjectStructureNode pathComponent = (ProjectStructureNode) path.getPathComponent(i);
            if (!pathComponent.getProjectStructureNodeType().equals(ProjectStructureNodeType.EMPTY)){
                paths.add(pathComponent);
            }
        }
        return paths.toArray(new ProjectStructureNode[]{});
    }

    public void updateTreeStructure(FileSystemChangeDTO fileSystemChangeDTO, ProjectStructureNode rootNode,
                                    ProjectStructureModel model){
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

    private void deleteNode(Path deletedFile, ProjectStructureNode rootNode, ProjectStructureModel model) {
        Path projectPath = applicationState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(deletedFile);
        boolean isDirectory = deletedFile.toFile().isDirectory();
        ProjectStructureNode node = getNodesForPathAndCreateOptionally(rootNode, fileRelativeToProjectPath, model, false, isDirectory);
        if (node!=null){
            model.removeNodeFromParent(node);
        }
    }


    private void findOrCreateNodesForPath (Path path, ProjectStructureNode rootNode, ProjectStructureModel model){
        Path projectPath = applicationState.getProjectPath().toPath();
        Path fileRelativeToProjectPath = projectPath.relativize(path);
        boolean isDirectory = path.toFile().isDirectory();
        ProjectStructureNode lastNode = getNodesForPathAndCreateOptionally(rootNode, fileRelativeToProjectPath, model, true, isDirectory);
        File file = path.toFile();
        if (file.isDirectory() && lastNode != null){
            for (File child : file.listFiles()) {
                createNodesForFileAndSubdirectories(child, lastNode, model, lastNode);
            }
        }
    }


    private ProjectStructureNode getNodesForPathAndCreateOptionally(ProjectStructureNode parentNode, Path filePath, ProjectStructureModel model, boolean createIfNotExist, boolean isDirectory) {

        Iterator<Path> pathIterator = filePath.iterator();
        if (!pathIterator.hasNext()){
            return null;
        }
        if (createIfNotExist){
            return model.getOrCreateChildWithPath(parentNode, filePath.toString(), isDirectory);
        }
        else{
            return model.getChildByPath(parentNode, filePath.toString()).orElse(null);
        }

    }

    private void createNodesForFileAndSubdirectories(File rootFile, ProjectStructureNode parent, ProjectStructureModel model, ProjectStructureNode lastNode) {
        boolean isDirectory = rootFile.isDirectory();
        ProjectStructureNode childNode = createProjectStructureNode(lastNode.getClassOrigin(), isDirectory, rootFile.getName(), rootFile.getName(), lastNode.isInsideJavaSources());

        if (model.getChildByPath(parent, rootFile.getName()).isEmpty()){
            model.insertNodeInto(childNode, parent, parent.getChildCount());
        }
        if(isDirectory){
            for (File file : rootFile.listFiles()) {
                createNodesForFileAndSubdirectories(file, childNode, model, lastNode);
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
        List<ProjectStructureNode[]> nodeNamesList = new ArrayList<>();
        Point point = null;
        for (TreePath selectionPath : selectionPaths) {
            point = tree.getPathBounds(selectionPath).getLocation();
            ProjectStructureNode[] nodeNames = getNodes(selectionPath);
            nodeNamesList.add(nodeNames);
        }
        return new ProjectStructureSelectionContextDTO(selectionPaths, nodeNamesList, point, null);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

    public ProjectStructureNode createEmptyRootNode() {
        return new ProjectStructureNode(ClassOrigin.SOURCES, ProjectStructureNodeType.EMPTY, "No projects loaded", "", false);
    }
}
