package core.nodehandling;

import com.github.javaparser.Position;
import core.uibuilders.ClassStructureNodesHandler;
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

    private ClassStructureNodesHandler classStructureNodesHandler;

    public ClassStructureNodeClickListener(UIEventsQueue uiEventsQueue, ClassStructureNodesHandler classStructureNodesHandler) {
        this.uiEventsQueue = uiEventsQueue;
        this.classStructureNodesHandler = classStructureNodesHandler;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {

            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            String [] paths = new String [path.getPathCount()];
            for (int i=0; i<paths.length; i++){
                DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
                Position position = classStructureNodesHandler.getPositionInTextEditorForClassNode(pathComponent);
                uiEventsQueue.dispatchEvent(UIEventType.CLASS_STRUCTURE_NODE_CLICKED, position);
            }
        }
    }
}
