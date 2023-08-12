package root.core.dto;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ProjectStructureSelectionContextDTO {

    private Point position;

    private TreePath[] selectionPaths;

    private List<ProjectStructureTreeElementDTO[]> nodePathsForEachSelectedItem;

    private File selectedFile;

    public ProjectStructureSelectionContextDTO(TreePath[] treePath, List<ProjectStructureTreeElementDTO[]> nodePathsForEachSelectedItem, Point position, File selectedFile) {
        this.selectionPaths = treePath;
        this.nodePathsForEachSelectedItem = nodePathsForEachSelectedItem;
        this.position = position;
        this.selectedFile = selectedFile;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public TreePath[] getSelectedPaths() {
        return selectionPaths;
    }

    public List<ProjectStructureTreeElementDTO[]> getNodesPaths() {
        return nodePathsForEachSelectedItem;
    }
}
