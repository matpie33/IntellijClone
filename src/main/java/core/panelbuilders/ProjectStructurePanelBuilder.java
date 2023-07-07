package core.panelbuilders;

import core.backend.DirectoryChangesDetector;
import core.context.ContextConfiguration;
import core.context.actionlisteners.FileDeleteKeyPressListener;
import core.context.providers.NodePathManipulation;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileSystemChangeDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;

@Component
public class ProjectStructurePanelBuilder implements UIEventObserver {

    private JTree projectStructureTree;

    private JPanel projectStructurePanel;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;

    private ContextConfiguration contextConfiguration;

    private FileDeleteKeyPressListener fileDeleteKeyPressListener;

    private ApplicatonState applicatonState;

    private DirectoryChangesDetector directoryChangesDetector;

    private NodePathManipulation nodePathManipulation;

    public ProjectStructurePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextConfiguration contextConfiguration, FileDeleteKeyPressListener fileDeleteKeyPressListener, ApplicatonState applicatonState, DirectoryChangesDetector directoryChangesDetector, NodePathManipulation nodePathManipulation) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextConfiguration = contextConfiguration;
        this.fileDeleteKeyPressListener = fileDeleteKeyPressListener;
        this.applicatonState = applicatonState;
        this.directoryChangesDetector = directoryChangesDetector;
        this.nodePathManipulation = nodePathManipulation;
    }

    @PostConstruct
    public void init (){
        projectStructurePanel = new JPanel(new BorderLayout());
        projectStructureTree = new JTree(new DefaultMutableTreeNode("Empty"));
        projectStructureTree.addMouseListener(new PopupMenuRequestListener(ContextType.PROJECT_STRUCTURE, contextConfiguration));
        projectStructureTree.addMouseListener(treeNodeDoubleClickListener);
        projectStructureTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        projectStructureTree.getActionMap().put("delete", fileDeleteKeyPressListener);
        projectStructureTree.addMouseListener(directoryChangesDetector);
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
    }

    public JPanel getPanel() {
        return projectStructurePanel;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        DefaultTreeModel model = (DefaultTreeModel) projectStructureTree.getModel();
        DefaultMutableTreeNode rootNode;
        switch (eventType) {
            case PROJECT_OPENED:
                rootNode = (DefaultMutableTreeNode) data;
                model.setRoot(rootNode);
                projectStructurePanel.revalidate();
                break;
            case FILE_REMOVED_FROM_PROJECT:
                ProjectStructureSelectionContextDTO context = (ProjectStructureSelectionContextDTO) data;
                model.removeNodeFromParent((MutableTreeNode) context.getTreePath().getLastPathComponent());
                projectStructurePanel.revalidate();
                break;
            case PROJECT_STRUCTURE_CHANGED:
                FileSystemChangeDTO fileSystemChangeDTO = (FileSystemChangeDTO) data;
                rootNode = (DefaultMutableTreeNode) projectStructureTree.getModel().getRoot();
                nodePathManipulation.updateTreeStructure(fileSystemChangeDTO, rootNode, model);
        }
    }

}
