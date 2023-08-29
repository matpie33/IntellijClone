package root.core.ui.tree;

import root.core.classmanipulating.ClassOrigin;
import root.core.constants.ClassType;

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
        return Optional.ofNullable(getChildWithOptionalCreate(parent, pathValue, false, false));
    }

    private ProjectStructureNode getChildWithOptionalCreate(ProjectStructureNode parent, String pathValue, boolean isFile, boolean createIfNotExists){
        Path path = Path.of(pathValue);
        ProjectStructureNode foundChild = parent;
        Iterator<Path> pathIterator = path.iterator();
        Path pathBuilder = Path.of("");
        while (pathIterator.hasNext()) {
            Path subPath = pathIterator.next();
            String subPathValue = subPath.toString();
            pathBuilder = pathBuilder.resolve(subPathValue);
            if (foundChild.getMergedNodes().contains(subPathValue)){
                continue;
            }
            Optional<ProjectStructureNode> childMaybe = findChildWithDisplayName(subPathValue, foundChild);
            if (childMaybe.isPresent()) {
                foundChild = childMaybe.get();
            } else {
                if (createIfNotExists){
                    foundChild = addChildWithPath(foundChild, pathBuilder.toString(), isFile, null);
                }
                else{
                    return null;
                }
            }

        }
        return foundChild;

    }

    public ProjectStructureNode getOrCreateChildWithPath(ProjectStructureNode parent, String pathValue, boolean isFile){
        return getChildWithOptionalCreate(parent, pathValue, isFile, true);

    }

    private Optional<ProjectStructureNode> findChildWithDisplayName (String subPathValue, ProjectStructureNode parent){
        for (int i = 0; i < parent.getChildCount(); i++) {
            ProjectStructureNode child = (ProjectStructureNode) parent.getChildAt(i);
            if (child.getDisplayName().equals(subPathValue) || child.getMergedNodes().contains(subPathValue)) {
                return Optional.of(child);
            }
        }
        return Optional.empty();

    }


    public ProjectStructureNode addChildWithPath(ProjectStructureNode parent, String pathValue, boolean isFile, ClassType classType) {

        if (!pathValue.equals(JAVA_SRC_DIRECTORY)){
            pathValue = pathValue.replace(JAVA_SRC_DIRECTORY, "");
        }
        List<String> newNodeSubPaths = splitPathToSubPaths(pathValue);
        boolean parentHasChildren = parent.getChildCount() >0;
        boolean isInJavaSources = isInJavaSources(parent);
        boolean parentHasMergedNodes = parent.getMergedNodes().size() > 1;
        boolean newNodeContainsAllMergedPathsFromParent = new HashSet<>(newNodeSubPaths).containsAll(parent.getMergedNodes());
        if (isInJavaSources && parentHasMergedNodes && !newNodeContainsAllMergedPathsFromParent)  {
            return splitNode(parent, newNodeSubPaths, isFile, classType);
        }
        if (isFile || !isInJavaSources){
            return addToParent(parent, pathValue, isFile, isInJavaSources, classType);
        }
        if (!parentHasChildren) {
            List<String> nodes = List.of(newNodeSubPaths.get(newNodeSubPaths.size()-1));
            addToMergedNodes(parent, nodes);
            return parent;
        }
        return addToParent(parent, pathValue, false, true, classType);

    }

    private ProjectStructureNode splitNode(ProjectStructureNode parentNode, List<String> newNodesSubPaths, boolean isFile, ClassType classType) {
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

        boolean isInJavaSources = true;
        if (!nonMatchingNodesFromParent.isEmpty()){
            List<TreeNode> nodesToCopy = new ArrayList<>();
            parentNode.children().asIterator().forEachRemaining(nodesToCopy::add);
            ProjectStructureNode nodeToAdd = addToParent(parentNode, "", isFile, isInJavaSources, classType);
            nodeToAdd.clearMergedNodes();
            addToMergedNodes(nodeToAdd, nonMatchingNodesFromParent);
            nodesToCopy.forEach(currentNode-> {
                ProjectStructureNode currentProjectStructureNode = (ProjectStructureNode) currentNode;
                boolean currentNodeIsFile = currentProjectStructureNode.getProjectStructureNodeType().equals(ProjectStructureNodeType.FILE);
                findIndexForNewNode(nodeToAdd, currentNodeIsFile, currentProjectStructureNode.getDisplayName());
                super.insertNodeInto((MutableTreeNode) currentNode, nodeToAdd, nodeToAdd.getChildCount());
            });
        }
        if (!nonMatchingNodesFromNewNode.isEmpty()){
            String pathName = String.join("/", nonMatchingNodesFromNewNode);
            return addToParent(parentNode,  pathName, isFile, isInJavaSources, classType);
        }
        throw new IllegalArgumentException();
    }

    private void addToMergedNodes(ProjectStructureNode parent, List<String> nodes) {
        nodes.forEach(parent::addMergedNode);
        List<String> mergedNodes = parent.getMergedNodes();
        parent.setDisplayName(String.join(".", mergedNodes));
        parent.setFilePath(String.join("/", mergedNodes));
    }

    private ProjectStructureNode addToParent(ProjectStructureNode parent, String pathValue, boolean isFile, boolean isInJavaSources, ClassType classType) {

        String lastPath = "";
        for (Path path : Path.of(pathValue)) {
            lastPath= path.toString();
        }
        ProjectStructureNodeType nodeType;
        if (classType!=null){
            nodeType = ProjectStructureNodeType.valueOf(classType.toString());
        }
        else {
            nodeType = isFile? ProjectStructureNodeType.FILE : ProjectStructureNodeType.DIRECTORY;
        }
        ProjectStructureNode node = new ProjectStructureNode(parent.getClassOrigin(),
                nodeType, lastPath, lastPath, isInJavaSources);
        int indexForNode = findIndexForNewNode(parent, isFile, lastPath);
        super.insertNodeInto(node, parent, indexForNode);
        return node;
    }

    private int findIndexForNewNode(ProjectStructureNode parent, boolean isFile, String newNodeName) {
        int indexForNode = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            ProjectStructureNode child = (ProjectStructureNode) parent.getChildAt(i);
            ProjectStructureNodeType childType = child.getProjectStructureNodeType();
            String childName = child.getDisplayName();

            if (!isFile){
                if (!childType.equals(ProjectStructureNodeType.DIRECTORY) || newNodeName.compareTo(childName) < 0){
                    indexForNode = i;
                    break;
                }
            }
            else if (childType.equals(ProjectStructureNodeType.FILE) && newNodeName.compareTo(childName) <0){
                indexForNode = i;
                break;
            }
            else if (!childType.equals(ProjectStructureNodeType.DIRECTORY) && !childType.equals(ProjectStructureNodeType.FILE)){
                break;
            }

            indexForNode++;
        }
        return indexForNode;
    }

    private boolean isInJavaSources(ProjectStructureNode parent) {
        HashSet<String> pathNodeNames = Arrays.stream(parent.getPath()).map(node -> ((ProjectStructureNode) node).getDisplayName()).collect(Collectors.toCollection(HashSet::new));
        boolean isInsideSrcMainJava = pathNodeNames.containsAll(Arrays.asList("src", "main", "java")) && !parent.getDisplayName().equals("java");
        return parent.isInsideJavaSources() || isInsideSrcMainJava || ( Arrays.asList(ClassOrigin.JDK, ClassOrigin.MAVEN).contains(parent.getClassOrigin())
                && !parent.getProjectStructureNodeType().equals(ProjectStructureNodeType.JAR) && !parent.isRoot());
    }

    private List<String> splitPathToSubPaths(String pathValue) {
        Path path = Path.of(pathValue);
        List<String> subPaths = new ArrayList<>();
        path.forEach(subPath->subPaths.add(subPath.toString()));
        return subPaths;
    }

}
