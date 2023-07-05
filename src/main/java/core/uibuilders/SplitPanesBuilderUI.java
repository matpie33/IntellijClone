package core.uibuilders;

import com.github.javaparser.Position;
import core.backend.FileAutoSaver;
import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.ui.components.ContextMenu;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

@Component
public class SplitPanesBuilderUI extends UIEventObserver {

    private JPanel projectStructurePanel;

    private ContextMenuValues contextMenuValues;

    private JTextArea editorText;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;
    private JTree projectStructureTree;

    private FileAutoSaver fileAutoSaver;

    private ClassStructureBuilderUI classStructureBuilderUI;

    private ClassStructurePanelBuilderUI classStructurePanelBuilderUI;


    public SplitPanesBuilderUI(UIEventsQueue uiEventsQueue, ContextMenuValues contextMenuValues, TreeNodeDoubleClickListener treeNodeDoubleClickListener, FileAutoSaver fileAutoSaver, ClassStructureBuilderUI classStructureBuilderUI, ClassStructurePanelBuilderUI classStructurePanelBuilderUI) {
        super(uiEventsQueue);
        this.contextMenuValues = contextMenuValues;
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;

        this.fileAutoSaver = fileAutoSaver;
        this.classStructureBuilderUI = classStructureBuilderUI;
        this.classStructurePanelBuilderUI = classStructurePanelBuilderUI;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        projectStructurePanel = new JPanel(new BorderLayout());
        createFileTree();
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
        JPanel code = new JPanel(new BorderLayout());

        JPanel console = new JPanel(new BorderLayout());
        JPanel classStructurePanel = classStructurePanelBuilderUI.getPanel();


        editorText = new JTextArea("code here");
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
            }
        });
        addMouseReleasedListener(editorText, ContextType.FILE_EDITOR);
        JTextArea consoleLabel = new JTextArea("console goes here");
        addMouseReleasedListener(consoleLabel, ContextType.CONSOLE);

        code.add(editorText, BorderLayout.CENTER);

        console.add(consoleLabel, BorderLayout.CENTER);

        JSplitPane horizontalLeftPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectStructurePanel, code);
        JSplitPane horizontalRightPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, horizontalLeftPart, classStructurePanel);
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
                FileReadResultDTO resultDTO = (FileReadResultDTO)data;
                editorText.setText(String.join(System.lineSeparator(), resultDTO.getLines()));
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                try {
                    Position lineStart = (Position)data;
                    editorText.setCaretPosition(editorText.getLineStartOffset(lineStart.line - 1) + lineStart.column-1);
                    editorText.requestFocus();
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
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
        return new HashSet<>(List.of(UIEventType.PROJECT_OPENED, UIEventType.FILE_OPENED_FOR_EDIT, UIEventType.CLASS_STRUCTURE_NODE_CLICKED));
    }

}
