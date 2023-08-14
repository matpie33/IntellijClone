package root.core.nodehandling;

import com.github.javaparser.Position;
import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

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
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Position position = classStructureNodesHandler.getPositionInTextEditorForClassNode(node);
            uiEventsQueue.dispatchEvent(UIEventType.CLASS_STRUCTURE_NODE_CLICKED, position);
        }
    }
}
