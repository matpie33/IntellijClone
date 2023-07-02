package core.uibuilders;

import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.ui.components.ContextMenu;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SplitPanesBuilderUI extends UIEventObserver {

    private JPanel projectStructurePanel;

    private ContextMenuValues contextMenuValues;

    private JTextArea editorText;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;
    private JTree projectStructureTree;


    public SplitPanesBuilderUI(UIEventsQueue uiEventsQueue,ContextMenuValues contextMenuValues, TreeNodeDoubleClickListener treeNodeDoubleClickListener) {
        super(uiEventsQueue);
        this.contextMenuValues = contextMenuValues;
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        projectStructurePanel = new JPanel(new BorderLayout());
        createFileTree();
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
        JPanel code = new JPanel(new BorderLayout());
        JPanel structure = new JPanel(new BorderLayout());
        JPanel console = new JPanel(new BorderLayout());

        editorText = new JTextArea("code here");
        addMouseReleasedListener(editorText, ContextType.FILE_EDITOR);
        JTextArea structureLabel = new JTextArea("class structure");
        addMouseReleasedListener(structureLabel, ContextType.FILE_STRUCTURE);
        JTextArea consoleLabel = new JTextArea("console goes here");
        addMouseReleasedListener(consoleLabel, ContextType.CONSOLE);

        code.add(editorText, BorderLayout.CENTER);
        structure.add(structureLabel, BorderLayout.CENTER);
        console.add(consoleLabel, BorderLayout.CENTER);

        JSplitPane horizontalLeftPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectStructurePanel, code);
        JSplitPane horizontalRightPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, horizontalLeftPart, structure);
        JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalRightPart, console);
        horizontalLeftPart.setResizeWeight(0.3);
        horizontalRightPart.setResizeWeight(0.7);
        rootSplit.setResizeWeight(0.8);

        panel.add(rootSplit, BorderLayout.CENTER);
        return  panel;
    }

    private void createFileTree() {
        projectStructureTree = new JTree(new DefaultMutableTreeNode("Empty"));
        addMouseReleasedListener(projectStructureTree, ContextType.PROJECT_STRUCTURE);
        projectStructureTree.addMouseListener(treeNodeDoubleClickListener);
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case PROJECT_OPENED:
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)data;
                DefaultTreeModel model = (DefaultTreeModel) projectStructureTree.getModel();
                model.setRoot(rootNode);
                projectStructurePanel.revalidate();
                break;
            case FILE_OPENED_FOR_EDIT:
                @SuppressWarnings("unchecked")
                List<String> content = (List<String>)data;
                editorText.setText(String.join("\n", content));
                break;
        }

    }

    private void addMouseReleasedListener(JComponent component, ContextType contextType) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    List<String> values = contextMenuValues.getValues(contextType);
                    ContextMenu contextMenu = new ContextMenu(values);
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

        });
    }

    @Override
    public Set<UIEventType> handledEventTypes() {
        return new HashSet<>(List.of(UIEventType.PROJECT_OPENED, UIEventType.FILE_OPENED_FOR_EDIT));
    }

}
