package core;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        JPanel iconsPanel = createIconsPanel();
        navigationPanel.add(iconsPanel, BorderLayout.EAST);

        return navigationPanel;

    }

    private JPanel createIconsPanel() {
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        iconsPanel.add(createIcon( "/build.png", "build"));
        iconsPanel.add(createIcon( "/run.png", "run"));
        iconsPanel.add(createIcon( "/debug.png", "debug"));
        iconsPanel.add(createIcon( "/runWithCoverage.png", "Run with coverage"));
        iconsPanel.add(createIcon( "/doProfile.png", "Profile <class> with 'Intellij profiler'"));
        iconsPanel.add(createIcon( "/stopDisabled.png", "Stop application"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(new JLabel("Git: "));
        iconsPanel.add(createIcon( "/gitUpdate.png", "Update project"));
        iconsPanel.add(createIcon( "/gitCommit.png", "Git commit"));
        iconsPanel.add(createIcon( "/gitPush.png", "Git push"));
        iconsPanel.add(createIcon( "/gitShowHistory.png", "Show history"));
        iconsPanel.add(createIcon( "/rollback.png", "Rollback"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(createIcon( "/searchEverywhere.png", "Search everywhere"));
        iconsPanel.add(createIcon( "/updatesAvailable.png", "Updates available"));
        iconsPanel.add(createIcon( "/spaceIcon.png", "Space: the integrated team environment"));
        return iconsPanel;
    }

    private static JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 16));
        return separator;
    }

    private JButton createIcon(String path, String tooltip) {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(path));
        JButton button = new JButton(imageIcon);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setOpaque(true);
        button.setToolTipText(tooltip);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                int r = 210;
                button.setBackground(new Color(r, r, r));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(null);
            }
        });
        return button;
    }

}
