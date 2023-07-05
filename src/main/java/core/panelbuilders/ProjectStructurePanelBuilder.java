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
public class ProjectStructurePanelBuilder implements UIEventObserver {

    private JTree projectStructureTree;

    private JPanel projectStructurePanel;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;

    private ContextMenuValues contextMenuValues;

    public ProjectStructurePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextMenuValues contextMenuValues) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextMenuValues = contextMenuValues;
    }

    @PostConstruct
    public void init (){
        projectStructurePanel = new JPanel(new BorderLayout());
        projectStructureTree = new JTree(new DefaultMutableTreeNode("Empty"));
        projectStructureTree.addMouseListener(new PopupMenuRequestListener(ContextType.PROJECT_STRUCTURE, contextMenuValues));
        projectStructureTree.addMouseListener(treeNodeDoubleClickListener);
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
    }

    public JPanel getPanel() {
        return projectStructurePanel;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType) {
            case PROJECT_OPENED:
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) data;
                DefaultTreeModel model = (DefaultTreeModel) projectStructureTree.getModel();
                model.setRoot(rootNode);
                projectStructurePanel.revalidate();
                break;
        }
    }
}
