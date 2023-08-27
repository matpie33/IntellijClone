package root.core.ui.tree;

import root.core.classmanipulating.ClassOrigin;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class ProjectStructureNode extends DefaultMutableTreeNode {

    private ClassOrigin classOrigin;

    private ProjectStructureNodeType projectStructureNodeType;

    private String displayName;

    private String filePath;

    private boolean isInsideJavaSources;

    private List<String> mergedNodes = new ArrayList<>();

    public ProjectStructureNode(ClassOrigin classOrigin, ProjectStructureNodeType projectStructureNodeType, String displayName, String filePath, boolean isInsideJavaSources) {
        this.classOrigin = classOrigin;
        this.projectStructureNodeType = projectStructureNodeType;
        this.displayName = displayName;
        this.filePath = filePath;
        this.isInsideJavaSources = isInsideJavaSources;
        if (!displayName.isEmpty()){
            mergedNodes.add(displayName);
        }
    }

    public void addMergedNode (String nodeName){
        mergedNodes.add(nodeName);
    }

    public List<String> getMergedNodes() {
        return mergedNodes;
    }

    public ClassOrigin getClassOrigin() {
        return classOrigin;
    }

    public ProjectStructureNodeType getProjectStructureNodeType() {
        return projectStructureNodeType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isInsideJavaSources() {
        return isInsideJavaSources;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public void clearMergedNodes() {
        mergedNodes.clear();
    }
}
