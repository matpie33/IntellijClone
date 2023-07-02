package core.mouselisteners;

import core.backend.FileReader;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Component
public class TreeNodeDoubleClickListener extends MouseAdapter {

    private FileReader fileReader;

    private UIEventsQueue uiEventsQueue;

    public TreeNodeDoubleClickListener(FileReader fileReader, UIEventsQueue uiEventsQueue) {
        this.fileReader = fileReader;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            String [] paths = new String [path.getPathCount()];
            for (int i=0; i<paths.length; i++){
                DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) path.getPathComponent(i);
                paths[i] =(String) pathComponent.getUserObject();
            }
            List<String> content = fileReader.read(paths);
            uiEventsQueue.handleEvent(UIEventType.FILE_OPENED_FOR_EDIT, content);
        }
    }
}