package core.context.providers;

import core.context.actionlisteners.ContextActionListener;
import core.contextMenu.ContextType;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

@Component
public class NodePathExtractor implements ContextProvider {

    @Override
    public Object getContext (MouseEvent e){

            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            String [] paths = new String [path.getPathCount()];
            for (int i=0; i<paths.length; i++){
                DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
                paths[i] =(String) pathComponent.getUserObject();
            }
            return new ProjectStructureSelectionContextDTO(path, paths);

    }

    @Override
    public ContextType getContextType() {
        return ContextType.PROJECT_STRUCTURE;
    }

}
