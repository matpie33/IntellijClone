package core.context.providers;

import core.contextMenu.ContextType;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

@Component
public class NodePathExtractor implements ContextProvider<ProjectStructureSelectionContextDTO> {

    @Override
    public ProjectStructureSelectionContextDTO getContext (MouseEvent e){

        JTree tree = (JTree) e.getSource();
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        String[] paths = extractPaths(path);
        return new ProjectStructureSelectionContextDTO(path, paths);

    }

    private String[] extractPaths(TreePath path) {
        String [] paths = new String [path.getPathCount()];
        for (int i=0; i<paths.length; i++){
            DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
            paths[i] =(String) pathComponent.getUserObject();
        }
        return paths;
    }

    @Override
    public ProjectStructureSelectionContextDTO getContext(ActionEvent actionEvent){
        JTree tree = (JTree) actionEvent.getSource();
        TreePath selectionPath = tree.getSelectionPath();
        String[] nodeNames;
        if (selectionPath != null){
            nodeNames = extractPaths(selectionPath);
        }
        else{
            nodeNames = new String [] {};
        }
        return new ProjectStructureSelectionContextDTO(selectionPath, nodeNames);

    }


    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

}
