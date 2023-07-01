package core;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

@Component
public class MainUI {

    private JPanel mainPanel;

    @PostConstruct
    public void init (){
        createMainPanel();
        JPanel navigationPanel = createNavigationPanel();
        JPanel splitPanes = createSplitPanesRootPanel();
        mainPanel.add(navigationPanel, BorderLayout.PAGE_START);
        mainPanel.add(splitPanes, BorderLayout.CENTER);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel createSplitPanesRootPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel fileTree = new JPanel(new BorderLayout());
        JPanel code = new JPanel(new BorderLayout());
        JPanel structure = new JPanel(new BorderLayout());
        JPanel console = new JPanel(new BorderLayout());

        JTextArea fileLabel = new JTextArea("file tree");
        JTextArea codeLabel = new JTextArea("code here");
        JTextArea structureLabel = new JTextArea("class structure");
        JTextArea consoleLabel = new JTextArea("console goes here");

        fileTree.add(fileLabel, BorderLayout.CENTER);
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

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout(5, 2));
        navigationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JLabel navigation = new JLabel("src>main>java>core>MainUI>createNavigationPanel");
        navigationPanel.add(navigation, BorderLayout.WEST);



        JPanel iconsPanel = new JPanel();
        iconsPanel.add(new JLabel("+"));
        iconsPanel.add(new JLabel("*"));
        iconsPanel.add(new JLabel("☺"));
        iconsPanel.add(new JLabel("♦"));

        navigationPanel.add(iconsPanel, BorderLayout.EAST);

        return navigationPanel;

    }

}
