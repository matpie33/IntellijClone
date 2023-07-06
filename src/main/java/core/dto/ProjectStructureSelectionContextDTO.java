package core.dto;

import javax.swing.tree.TreePath;
import java.util.List;

public class ProjectStructureSelectionContextDTO {

    private TreePath treePath;

    private String[] nodeNames;

    public ProjectStructureSelectionContextDTO(TreePath treePath, String[] nodeNames) {
        this.treePath = treePath;
        this.nodeNames = nodeNames;
    }


    public TreePath getTreePath() {
        return treePath;
    }

    public String[] getNodeNames() {
        return nodeNames;
    }
}
