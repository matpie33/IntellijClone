package root.core.ui.tree;

import root.core.classmanipulating.ClassOrigin;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProjectStructureNode extends DefaultMutableTreeNode {

    private ClassOrigin classOrigin;

    private ProjectStructureNodeType projectStructureNodeType;

    private String displayName;

    private String filePath;

    private boolean isFileNode = true;

    private boolean isInsideJavaSources;

    public ProjectStructureNode(ClassOrigin classOrigin, ProjectStructureNodeType projectStructureNodeType, String displayName, String filePath, boolean isInsideJavaSources) {
        this.classOrigin = classOrigin;
        this.projectStructureNodeType = projectStructureNodeType;
        this.displayName = displayName;
        this.filePath = filePath;
        this.isInsideJavaSources = isInsideJavaSources;
    }

    public ClassOrigin getClassOrigin() {
        return classOrigin;
    }

    public ProjectStructureNodeType getProjectStructureNodeType() {
        return projectStructureNodeType;
    }

    public ProjectStructureNode getOrCreateChild(DefaultTreeModel model, String displayName, ProjectStructureNodeType type){
        for (int i = 0; i < getChildCount(); i++) {
            ProjectStructureNode node = (ProjectStructureNode) getChildAt(i);
            if (node.getDisplayName().equals(displayName)){
                return node;
            }
        }
        ProjectStructureNode projectStructureNode = new ProjectStructureNode(classOrigin, type, displayName, filePath, isInsideJavaSources);
        model.insertNodeInto(projectStructureNode, this, getChildCount());
        return projectStructureNode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isFileNode() {
        return isFileNode;
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
}
