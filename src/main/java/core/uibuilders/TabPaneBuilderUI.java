package core.uibuilders;

import core.backend.FileIO;
import core.dto.FileReadResultDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class TabPaneBuilderUI {

    private JTabbedPane tabbedPane;

    private Map<File, JComponent> openedTabs = new HashMap<>();

    private UIEventsQueue uiEventsQueue;

    private FileIO fileIO;

    public TabPaneBuilderUI(UIEventsQueue uiEventsQueue, FileIO fileIO) {
        this.uiEventsQueue = uiEventsQueue;
        this.fileIO = fileIO;
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public boolean containsTab (File file){
        return openedTabs.containsKey(file);
    }

    public java.awt.Component getActiveTabContent(){
        return tabbedPane.getSelectedComponent();
    }

    public void addTab(JComponent content, File file) {
        tabbedPane.add(content);
        JPanel tabHeaderPanel = createTabHeader( content, file.getName());
        tabHeaderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FileReadResultDTO fileReadResultDTO = fileIO.readFile(file.toPath());
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, fileReadResultDTO);
            }
        });
        int index = tabbedPane.indexOfComponent(content);
        tabbedPane.setTabComponentAt(index, tabHeaderPanel);
        tabbedPane.setSelectedIndex(index);
        String tooltip = file.toString();
        tabHeaderPanel.setToolTipText(tooltip);
        Container parent = tabHeaderPanel.getParent();
        ((JComponent) parent).setToolTipText(tooltip);
        openedTabs.put(file, content);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private JPanel createTabHeader(final JComponent tabContentPanel, String title)
    {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JButton closeButton = createCloseButton(tabbedPane, tabContentPanel, title);

        titlePanel.add(titleLabel);
        titlePanel.add(closeButton);

        return titlePanel;
    }

    private JButton createCloseButton(JTabbedPane tabbedPane, JComponent tabContentPanel, String tabName) {
        JButton closeButton = new JButton("x");
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFont(closeButton.getFont().deriveFont(15f));
        int size = 15;
        closeButton.setPreferredSize(new Dimension(size, size));

        closeButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setContentAreaFilled(false);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                tabbedPane.remove(tabContentPanel);
                openedTabs.remove(tabName);
            }
        });
        return closeButton;
    }

    public void selectTab(File file) {
        tabbedPane.setSelectedComponent(openedTabs.get(file));
    }
}
