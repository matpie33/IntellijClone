package root.ui.uibuilders;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.dto.ApplicationState;
import root.core.dto.FileReadResultDTO;
import root.core.fileio.FileAutoSaver;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.ui.components.EditorScrollPane;
import root.ui.components.SyntaxColorStyledDocument;

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

    private Map<File, EditorScrollPane> openedTabs = new HashMap<>();

    private UIEventsQueue uiEventsQueue;

    private ApplicationState applicationState;

    private FileAutoSaver fileAutoSaver;

    public TabPaneBuilderUI(UIEventsQueue uiEventsQueue, ApplicationState applicationState, FileAutoSaver fileAutoSaver) {
        this.uiEventsQueue = uiEventsQueue;
        this.applicationState = applicationState;
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

    public void addTab(EditorScrollPane scrollPane, File file, List<String> lines, ClassOrigin classOrigin) {
        tabbedPane.add(scrollPane);
        JPanel tabHeaderPanel = createTabHeader(scrollPane, file);
        tabHeaderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileAutoSaver.save();
                FileReadResultDTO readResult = new FileReadResultDTO();
                readResult.setFile(file);
                readResult.setContentLines(lines);
                readResult.setClassOrigin(classOrigin);
                readResult.setReadSuccessfully(true);
                readResult.setPathFromRoot(file.toString());
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

    public EditorScrollPane selectTab(File file, String text) {
        EditorScrollPane editorScrollPane = openedTabs.get(file);
        JTextPane textEditor = editorScrollPane.getTextEditor();
        editorScrollPane.setUpdateCaret(false);
        textEditor.setText(text);
        editorScrollPane.setUpdateCaret(true);
        tabbedPane.setSelectedComponent(editorScrollPane);
        editorScrollPane.revalidate();
        return editorScrollPane;
    }

    public void addTabChangeListener(ChangeListener changeListener) {
        tabbedPane.addChangeListener(changeListener);
    }
}
