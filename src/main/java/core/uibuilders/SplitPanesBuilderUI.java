package core.uibuilders;

import core.uievents.UIEventHandler;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SplitPanesBuilderUI implements UIEventHandler {

    private JPanel fileTree;

    public JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        fileTree = new JPanel(new BorderLayout());
        JPanel code = new JPanel(new BorderLayout());
        JPanel structure = new JPanel(new BorderLayout());
        JPanel console = new JPanel(new BorderLayout());

        JTextArea codeLabel = new JTextArea("code here");
        JTextArea structureLabel = new JTextArea("class structure");
        JTextArea consoleLabel = new JTextArea("console goes here");

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
        fileTree.add(new JScrollPane(data[0]));
        fileTree.revalidate();
    }

    @Override
    public Set<UIEventType> handledEventTypes() {
        return new HashSet<>(List.of(UIEventType.FILE_OPENED));
    }

}
