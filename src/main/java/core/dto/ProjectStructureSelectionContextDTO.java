package core.dto;

import javax.swing.tree.TreePath;
import java.util.List;

public class ProjectStructureSelectionContextDTO {

    private TreePath[] selectionPaths;

    private List<String[]> nodePathsForEachSelectedItem;

    public ProjectStructureSelectionContextDTO(TreePath[] treePath, List<String[]> nodePathsForEachSelectedItem) {
        this.selectionPaths = treePath;
        this.nodePathsForEachSelectedItem = nodePathsForEachSelectedItem;
    }


    public TreePath[] getSelectedPaths() {
        return selectionPaths;
    }

    public List<String[]> getNodesPaths() {
        return nodePathsForEachSelectedItem;
    }
}
