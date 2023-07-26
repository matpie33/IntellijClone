package core.panelbuilders;

import com.github.javaparser.Position;
import core.backend.FileAutoSaver;
import core.backend.FileIO;
import core.constants.FontsConstants;
import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.dto.FileSystemChangeDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.ui.components.EditorScrollPane;
import core.ui.components.SyntaxColorStyledDocument;
import core.uibuilders.TabPaneBuilderUI;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileEditorPanelBuilder implements UIEventObserver, ApplicationContextAware {


    private ContextConfiguration contextConfiguration;

    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    private Font editorFont = new Font("DejaVu Sans Mono", Font.PLAIN, FontsConstants.FONT_SIZE);


    private FileIO fileIO;

    private JPanel rootPanel;
    private TabPaneBuilderUI tabPaneBuilderUI;
    private ApplicationContext applicationContext;

    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState, FileIO fileIO, TabPaneBuilderUI tabPaneBuilderUI) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
        this.tabPaneBuilderUI = tabPaneBuilderUI;
    }

    @PostConstruct
    public void init (){
        rootPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = createScrollableTextEditor("");
        tabPaneBuilderUI.addTab( scrollPane, new File("untitled.java"), new ArrayList<>());
        rootPanel.add(tabPaneBuilderUI.getTabbedPane(), BorderLayout.CENTER);
    }

    private JScrollPane createScrollableTextEditor(String text) {
        SyntaxColorStyledDocument document = applicationContext.getBean(SyntaxColorStyledDocument.class);
        JTextPane editorText = new JTextPane(document) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        editorText.setCaret(new ImprovedCaret());
        editorText.getCaret().setBlinkRate(500);
        editorText.setFont(editorFont);
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
                applicatonState.addCurrentFileToClassesToRecompile();
            }
        });
        editorText.addMouseListener(new PopupMenuRequestListener(ContextType.FILE_EDITOR, contextConfiguration));

        document.initialize(editorFont, editorText);
        EditorScrollPane editorScrollPane = new EditorScrollPane(editorText);
        editorScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        editorScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        editorText.setText(text);
        return editorScrollPane;
    }

    public JPanel getPanel() {
        return rootPanel;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType) {
            case FILE_OPENED_FOR_EDIT:
                @SuppressWarnings("unchecked")
                FileReadResultDTO resultDTO = (FileReadResultDTO)data;
                openFile(resultDTO.getLines(), resultDTO.getFile());
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                Position lineStart = (Position)data;
                EditorScrollPane editorScrollPane = (EditorScrollPane) tabPaneBuilderUI.getActiveTabContent();
                JTextPane editorText = editorScrollPane.getTextEditor();
                Element rootElement = editorText.getDocument().getDefaultRootElement();
                editorText.setCaretPosition(rootElement.getElement(lineStart.line - 1).getStartOffset() + lineStart.column-1);
                editorText.requestFocus();
                break;
            case PROJECT_STRUCTURE_CHANGED:
                if (applicatonState.getOpenedFile()== null){
                    return;
                }
                FileSystemChangeDTO fileSystemChange = (FileSystemChangeDTO) data;
                List<Path> modifiedFiles = fileSystemChange.getModifiedFiles();
                Path openedFile = applicatonState.getOpenedFile().toPath();
                if (modifiedFiles.contains(openedFile)){
                    try {
                        List<String> content = fileIO.getContent(openedFile);
                        openFile(content, openedFile.toFile());
                        applicatonState.addCurrentFileToClassesToRecompile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    }

    private void openFile(List<String> lines, File file) {
        String text = String.join(System.lineSeparator(), lines);
        if (tabPaneBuilderUI.containsTab(file)){
            tabPaneBuilderUI.selectTab(file);
        }
        else{
            JScrollPane scrollPane = createScrollableTextEditor(text);
            tabPaneBuilderUI.addTab(scrollPane, file, lines);

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
