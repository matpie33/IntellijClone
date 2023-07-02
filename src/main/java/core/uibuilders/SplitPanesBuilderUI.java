package core.uibuilders;

import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.ui.components.ContextMenu;
import core.uievents.UIEventHandler;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SplitPanesBuilderUI implements UIEventHandler {

    private JPanel fileTree;

    private ContextMenuValues contextMenuValues;

    public SplitPanesBuilderUI(ContextMenuValues contextMenuValues) {
        this.contextMenuValues = contextMenuValues;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        fileTree = new JPanel(new BorderLayout());
        JPanel code = new JPanel(new BorderLayout());
        JPanel structure = new JPanel(new BorderLayout());
        JPanel console = new JPanel(new BorderLayout());

        JTextArea codeLabel = new JTextArea("code here");
        addMouseReleasedListener(codeLabel, ContextType.FILE_EDITOR);
        JTextArea structureLabel = new JTextArea("class structure");
        addMouseReleasedListener(structureLabel, ContextType.FILE_STRUCTURE);
        JTextArea consoleLabel = new JTextArea("console goes here");
        addMouseReleasedListener(consoleLabel, ContextType.CONSOLE);

        code.add(codeLabel, BorderLayout.CENTER);
        structure.add(structureLabel, BorderLayout.CENTER);
        console.add(consoleLabel, BorderLayout.CENTER);

        JSplitPane horizontalLeftPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTree, code);
        JSplitPane horizontalRightPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, horizontalLeftPart, structure);
        JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalRightPart, console);
        horizontalLeftPart.setResizeWeight(0.3);
        horizontalRightPart.setResizeWeight(0.7);
        rootSplit.setResizeWeight(0.8);

        panel.add(rootSplit, BorderLayout.CENTER);
        return  panel;
    }

    @Override
    public void handleEvent(UIEventType eventType, JComponent... data) {
        fileTree.removeAll();
        JComponent treeUI = data[0];
        addMouseReleasedListener(treeUI, ContextType.PROJECT_STRUCTURE);
        fileTree.add(new JScrollPane(treeUI));
        fileTree.revalidate();
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
        return new HashSet<>(List.of(UIEventType.FILE_OPENED));
    }

}
