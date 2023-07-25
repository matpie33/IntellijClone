package core.uibuilders;

import core.dto.*;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectStructureBuilderUI {

    private ApplicatonState applicatonState;

    public ProjectStructureBuilderUI(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
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
            DefaultMutableTreeNode jarNode = addJarNodeToMavenNode(model, mavenNode, fullPathToJar);

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

    private DefaultMutableTreeNode addJarNodeToMavenNode(DefaultTreeModel model, DefaultMutableTreeNode mavenNode, String fullPathToJar) {
        String pathFromRepoToJar = fullPathToJar.replace(applicatonState.getLocalRepositoryPath(), "").replace("\\", ".").replaceFirst(".", "");
        TreeNodeFileDTO jarNodeDTO = new TreeNodeFileDTO(TreeNodeFileDTO.Type.DIRECTORY, pathFromRepoToJar);
        DefaultMutableTreeNode jarNode = new DefaultMutableTreeNode(jarNodeDTO);
        model.insertNodeInto(jarNode, mavenNode, mavenNode.getChildCount());
        return jarNode;
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
}
