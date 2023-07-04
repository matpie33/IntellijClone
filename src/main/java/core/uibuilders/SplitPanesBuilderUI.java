package core.uibuilders;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
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
    private JTree fileStructureTree;
    private JPanel classStructurePanel;

    private ApplicatonState applicatonState;


    public SplitPanesBuilderUI(UIEventsQueue uiEventsQueue, ContextMenuValues contextMenuValues, TreeNodeDoubleClickListener treeNodeDoubleClickListener, FileAutoSaver fileAutoSaver, ClassStructureBuilderUI classStructureBuilderUI, ApplicatonState applicatonState) {
        super(uiEventsQueue);
        this.contextMenuValues = contextMenuValues;
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;

        this.fileAutoSaver = fileAutoSaver;
        this.classStructureBuilderUI = classStructureBuilderUI;
        this.applicatonState = applicatonState;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        projectStructurePanel = new JPanel(new BorderLayout());
        createFileTree();
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
        JPanel code = new JPanel(new BorderLayout());

        classStructurePanel = new JPanel(new BorderLayout());
        JPanel console = new JPanel(new BorderLayout());

        fileStructureTree = new JTree(new DefaultMutableTreeNode(""));
        classStructurePanel.add(new JScrollPane(fileStructureTree));
        editorText = new JTextArea("code here");
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
            }
        });
        addMouseReleasedListener(editorText, ContextType.FILE_EDITOR);
        addMouseReleasedListener(fileStructureTree, ContextType.FILE_STRUCTURE);
        JTextArea consoleLabel = new JTextArea("console goes here");
        addMouseReleasedListener(consoleLabel, ContextType.CONSOLE);

        code.add(editorText, BorderLayout.CENTER);
        classStructurePanel.add(fileStructureTree, BorderLayout.CENTER);
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
                if (resultDTO.isJavaFile()){
                    displayJavaFileStructure();
                }
                else{
                    DefaultTreeModel structureModel = (DefaultTreeModel) fileStructureTree.getModel();
                    structureModel.setRoot(new DefaultMutableTreeNode("N/a"));
                }
                break;
        }

    }

    private void displayJavaFileStructure() {
        try {
            DefaultTreeModel structureModel = (DefaultTreeModel) fileStructureTree.getModel();
            CompilationUnit compilationUnit = StaticJavaParser.parse(applicatonState.getOpenedFile());
            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().iterator().next();
            DefaultMutableTreeNode tree = classStructureBuilderUI.build(classDeclaration);
            structureModel.setRoot(tree);
            classStructurePanel.revalidate();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
