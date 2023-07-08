package core.dto;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class ProjectStructureSelectionContextDTO {

    private Point position;

    private TreePath[] selectionPaths;

    private List<String[]> nodePathsForEachSelectedItem;

    public ProjectStructureSelectionContextDTO(TreePath[] treePath, List<String[]> nodePathsForEachSelectedItem, Point position) {
        this.selectionPaths = treePath;
        this.nodePathsForEachSelectedItem = nodePathsForEachSelectedItem;
        this.position = position;
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

    public List<String[]> getNodesPaths() {
        return nodePathsForEachSelectedItem;
    }
}
