package core.mouselisteners;

import com.github.javaparser.Position;
import core.dto.NavigableTreeElementDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class ClassStructureNodeClickListener extends MouseAdapter {


    private UIEventsQueue uiEventsQueue;

    public ClassStructureNodeClickListener(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {

            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            String [] paths = new String [path.getPathCount()];
            for (int i=0; i<paths.length; i++){
                DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
                NavigableTreeElementDTO navigableElement = (NavigableTreeElementDTO) pathComponent.getUserObject();
                Position positionInClassFile = navigableElement.getStartingPosition();
                uiEventsQueue.dispatchEvent(UIEventType.CLASS_STRUCTURE_NODE_CLICKED, positionInClassFile);

            }
        }
    }
}
