package core.panelbuilders;

import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.mouselisteners.PopupMenuRequestListener;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

@Component
public class ConsolePanelBuilder  {


    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;

    private ContextMenuValues contextMenuValues;
    private JPanel panel;

    public ConsolePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextMenuValues contextMenuValues) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextMenuValues = contextMenuValues;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        JTextArea consoleLabel = new JTextArea("console goes here");
        panel.add(consoleLabel, BorderLayout.CENTER);
        consoleLabel.addMouseListener(new PopupMenuRequestListener(ContextType.CONSOLE, contextMenuValues));
    }

    public JPanel getPanel() {
        return panel;
    }

}
