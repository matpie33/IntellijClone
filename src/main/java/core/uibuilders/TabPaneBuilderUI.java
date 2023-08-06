package core.uibuilders;

import core.backend.FileAutoSaver;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.ui.components.EditorScrollPane;
import core.ui.components.SyntaxColorStyledDocument;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TabPaneBuilderUI {

    private JTabbedPane tabbedPane;

    private Map<File, JScrollPane> openedTabs = new HashMap<>();

    private UIEventsQueue uiEventsQueue;

    private ApplicatonState applicatonState;

    private FileAutoSaver fileAutoSaver;

    public TabPaneBuilderUI(UIEventsQueue uiEventsQueue, ApplicatonState applicatonState, FileAutoSaver fileAutoSaver) {
        this.uiEventsQueue = uiEventsQueue;
        this.applicatonState = applicatonState;
        this.fileAutoSaver = fileAutoSaver;
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public boolean containsTab (File file){
        return openedTabs.containsKey(file);
    }

    public EditorScrollPane getScrollPaneFromActiveTab(){
        return ((EditorScrollPane) tabbedPane.getSelectedComponent());
    }

    public void addTab(JScrollPane scrollPane, File file, List<String> lines) {
        tabbedPane.add(scrollPane);
        JPanel tabHeaderPanel = createTabHeader(scrollPane, file);
        tabHeaderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileAutoSaver.save();
                FileReadResultDTO readResult = new FileReadResultDTO();
                readResult.setFile(file);
                readResult.setLines(lines);
                readResult.setJavaFile(file.getName().endsWith(".java"));
                readResult.setReaded(true);
                readResult.setPathFromRoot(file.toString());
                readResult.setEditable(!file.getName().endsWith(".class"));
                applicatonState.setOpenedFile(file);
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, readResult);
            }
        });
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setTabComponentAt(index, tabHeaderPanel);
        tabbedPane.setSelectedIndex(index);
        String tooltip = file.toString();
        tabHeaderPanel.setToolTipText(tooltip);
        tabbedPane.setToolTipTextAt(index, tooltip);
        openedTabs.put(file, scrollPane);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private JPanel createTabHeader(final JComponent tabContentPanel, File file)
    {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(file.getName());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JButton closeButton = createCloseButton(tabbedPane, tabContentPanel, file);

        titlePanel.add(titleLabel);
        titlePanel.add(closeButton);

        return titlePanel;
    }

    private JButton createCloseButton(JTabbedPane tabbedPane, JComponent tabContentPanel, File file) {
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
                openedTabs.remove(file);
            }
        });
        return closeButton;
    }

    public SyntaxColorStyledDocument getDocumentForActiveEditor(){
        EditorScrollPane selectedComponent = (EditorScrollPane) tabbedPane.getSelectedComponent();
        return (SyntaxColorStyledDocument) selectedComponent.getTextEditor().getDocument();
    }

    public JTextPane getTextComponentFromActiveTab (){
        EditorScrollPane selectedComponent = (EditorScrollPane) tabbedPane.getSelectedComponent();
        return selectedComponent.getTextEditor();
    }

    public void selectTab(File file) {
        tabbedPane.setSelectedComponent(openedTabs.get(file));
    }

    public void addTabChangeListener(ChangeListener changeListener) {
        tabbedPane.addChangeListener(changeListener);
    }
}
