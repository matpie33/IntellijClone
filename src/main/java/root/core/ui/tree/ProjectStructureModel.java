package root.core.ui.tree;

import root.core.classmanipulating.ClassOrigin;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectStructureModel extends DefaultTreeModel {

    private static final String JAVA_SRC_DIRECTORY = "src\\main\\java\\";

    public ProjectStructureModel(TreeNode root) {
        super(root);
    }

    public Optional<ProjectStructureNode> getChildByPath(ProjectStructureNode parent, String pathValue ){
        Path path = Path.of(pathValue);
        ProjectStructureNode foundChild = parent;
        for (Path subPath : path) {
            String subPathValue = subPath.toString();
            if (foundChild.getMergedNodes().contains(subPathValue)) {
                continue;
            }
            boolean isFoundChildInIteration = false;
            for (int i = 0; i < foundChild.getChildCount(); i++) {
                ProjectStructureNode child = (ProjectStructureNode) foundChild.getChildAt(i);
                if (child.getDisplayName().equals(subPathValue) || child.getMergedNodes().contains(subPathValue)) {
                    foundChild = child;
                    isFoundChildInIteration = true;
                    break;
                }
            }
            if (!isFoundChildInIteration) {
                return Optional.empty();
            }
        }
        return Optional.of(foundChild);
    }

    public ProjectStructureNode getOrCreateChildWithPath(ProjectStructureNode parent, String pathValue, boolean isFile){
        Path path = Path.of(pathValue);
        ProjectStructureNode foundChild = parent;
        Iterator<Path> pathIterator = path.iterator();
        Path pathBuilder = Path.of("");
        while (pathIterator.hasNext()) {
            Path subPath = pathIterator.next();
            String subPathValue = subPath.toString();
            pathBuilder = pathBuilder.resolve(subPathValue);
            boolean isFoundChildInIteration = false;
            if (foundChild.getMergedNodes().contains(subPathValue)){
                continue;
            }
            for (int i = 0; i < foundChild.getChildCount(); i++) {
                ProjectStructureNode child = (ProjectStructureNode) foundChild.getChildAt(i);
                if (child.getDisplayName().equals(subPathValue) || child.getMergedNodes().contains(subPathValue)) {
                    foundChild = child;
                    isFoundChildInIteration = true;
                    break;
                }
            }
            if (!isFoundChildInIteration) {
                foundChild = addChildWithPath(foundChild, pathBuilder.toString(), isFile);
            }

        }
        return foundChild;

    }


    public ProjectStructureNode addChildWithPath(ProjectStructureNode parent, String pathValue, boolean isFile) {

        if (!pathValue.equals(JAVA_SRC_DIRECTORY)){
            pathValue = pathValue.replace(JAVA_SRC_DIRECTORY, "");
        }
        List<String> subPaths = splitPathToSubPaths(pathValue);
        boolean parentHasDirectories = parent.getChildCount() >0;
        boolean isInJavaSources = isInJavaSources(parent);
        boolean parentHasMergedNodes = parent.getMergedNodes().size() > 1;
        boolean newNodeContainsAllMergedPathsFromParent = new HashSet<>(subPaths).containsAll(parent.getMergedNodes());
        if (isInJavaSources && parentHasMergedNodes && !newNodeContainsAllMergedPathsFromParent)  {
            return splitNode(parent, subPaths, false, true);
        }
        if (isFile || !isInJavaSources){
            return addToParent(parent, pathValue, isFile, isInJavaSources);
        }
        if (!parentHasDirectories) {
            List<String> nodes = List.of(subPaths.get(subPaths.size()-1));
            addToMergedNodes(parent, nodes);
            return parent;
        }
        return addToParent(parent, pathValue, false, true);

    }

    private boolean nodeHasDirectories (ProjectStructureNode node){
        for (int i = 0; i < node.getChildCount(); i++) {
            ProjectStructureNode child = (ProjectStructureNode) node.getChildAt(i);
            if (child.getProjectStructureNodeType().equals(ProjectStructureNodeType.DIRECTORY)){
                return true;
            }
        }
        return false;
    }

    private ProjectStructureNode splitNode(ProjectStructureNode parentNode, List<String> newNodesSubPaths, boolean isFile, boolean isInJavaSources) {
        List<String> parentMergedNodes = parentNode.getMergedNodes();
        List<String> matchingNodes = new ArrayList<>();
        List<String> nonMatchingNodesFromParent = new ArrayList<>();
        List<String> nonMatchingNodesFromNewNode = new ArrayList<>();

        int i =0;
        for (String subPath : newNodesSubPaths) {
            if (i<parentMergedNodes.size()){
                if (parentMergedNodes.get(i).equals(subPath)){
                    i++;
                    matchingNodes.add(subPath);
                }
                else if (!matchingNodes.isEmpty()){
                    nonMatchingNodesFromNewNode.add(subPath);
                }
            }
            else{
                nonMatchingNodesFromNewNode.add(subPath);
            }
        }

        while (i<parentMergedNodes.size()){
            nonMatchingNodesFromParent.add(parentMergedNodes.get(i));
            i++;
        }

        parentNode.clearMergedNodes();
        addToMergedNodes(parentNode, matchingNodes);

        if (!nonMatchingNodesFromParent.isEmpty()){
            List<TreeNode> nodesToCopy = new ArrayList<>();
            parentNode.children().asIterator().forEachRemaining(nodesToCopy::add);
            ProjectStructureNode node = addToParent(parentNode, "", isFile, isInJavaSources);
            node.clearMergedNodes();
            addToMergedNodes(node, nonMatchingNodesFromParent);
            nodesToCopy.forEach(nodeToCopy->super.insertNodeInto((MutableTreeNode) nodeToCopy,node, node.getChildCount()));
        }
        if (!nonMatchingNodesFromNewNode.isEmpty()){
            String pathName = String.join("/", nonMatchingNodesFromNewNode);
            return addToParent(parentNode,  pathName, isFile, isInJavaSources);
        }
        throw new IllegalArgumentException();
    }

    private void addToMergedNodes(ProjectStructureNode parent, List<String> nodes) {
        nodes.forEach(parent::addMergedNode);
        List<String> mergedNodes = parent.getMergedNodes();
        parent.setDisplayName(String.join(".", mergedNodes));
        parent.setFilePath(String.join("/", mergedNodes));
    }

    private ProjectStructureNode addToParent(ProjectStructureNode parent, String pathName, boolean isFile, boolean isInJavaSources) {

        String lastPath = "";
        for (Path path : Path.of(pathName)) {
            lastPath= path.toString();
        }
        ProjectStructureNode node = new ProjectStructureNode(parent.getClassOrigin(),
                isFile ? ProjectStructureNodeType.FILE : ProjectStructureNodeType.DIRECTORY, lastPath, lastPath, isInJavaSources);
        super.insertNodeInto(node, parent, parent.getChildCount());
        return node;
    }

    private boolean isInJavaSources(ProjectStructureNode parent) {
        HashSet<String> pathNodeNames = Arrays.stream(parent.getPath()).map(node -> ((ProjectStructureNode) node).getDisplayName()).collect(Collectors.toCollection(HashSet::new));
        boolean isInsideSrcMainJava = pathNodeNames.containsAll(Arrays.asList("src", "main", "java")) && !parent.getDisplayName().equals("java");
        return parent.isInsideJavaSources() || isInsideSrcMainJava || ( Arrays.asList(ClassOrigin.JDK, ClassOrigin.MAVEN).contains(parent.getClassOrigin())
                && !parent.getProjectStructureNodeType().equals(ProjectStructureNodeType.JAR) && !parent.isRoot());
    }

    private List<String> splitPathToSubPaths(String pathName) {
        Path path = Path.of(pathName);
        List<String> subPaths = new ArrayList<>();
        path.forEach(subPath->subPaths.add(subPath.toString()));
        return subPaths;
    }



}
