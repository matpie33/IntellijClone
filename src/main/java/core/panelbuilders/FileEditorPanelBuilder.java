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
import core.keylisteners.CodeCompletionNavigator;
import core.mouselisteners.PopupMenuRequestListener;
import core.shortcuts.FileEditorShortcuts;
import core.ui.components.CodeCompletionPopup;
import core.ui.components.EditorScrollPane;
import core.ui.components.FileEditorComponent;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private FileEditorShortcuts fileEditorShortcuts;

    private CodeCompletionPopup codeCompletionPopup;

    private CodeCompletionNavigator codeCompletionNavigator;


    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState, FileIO fileIO, TabPaneBuilderUI tabPaneBuilderUI, FileEditorShortcuts fileEditorShortcuts, CodeCompletionPopup codeCompletionPopup, CodeCompletionNavigator codeCompletionNavigator) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
        this.tabPaneBuilderUI = tabPaneBuilderUI;
        this.fileEditorShortcuts = fileEditorShortcuts;
        this.codeCompletionPopup = codeCompletionPopup;
        this.codeCompletionNavigator = codeCompletionNavigator;
    }

    @PostConstruct
    public void init (){
        rootPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = createScrollableTextEditor("", true);
        tabPaneBuilderUI.addTab( scrollPane, new File("untitled.java"), new ArrayList<>());
        JTabbedPane tabbedPane = tabPaneBuilderUI.getTabbedPane();
        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        fileEditorShortcuts.assignShortcuts(tabbedPane);
    }

    private JScrollPane createScrollableTextEditor(String text, boolean editable) {
        SyntaxColorStyledDocument document = applicationContext.getBean(SyntaxColorStyledDocument.class);
        FileEditorComponent editorText = new FileEditorComponent (document);
        editorText.setEditable(editable);
        editorText.setCaret(new ImprovedCaret());
        editorText.getCaret().setBlinkRate(500);
        editorText.setFont(editorFont);
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
                applicatonState.addCurrentFileToClassesToRecompile();
                codeCompletionNavigator.handleCodeCompletionNavigation(e);
            }
        });
        editorText.addMouseListener(new PopupMenuRequestListener(ContextType.FILE_EDITOR, contextConfiguration));
        editorText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                codeCompletionPopup.hide();
                document.clearWordBeingTyped();
            }
        });

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
                openFile(resultDTO.getLines(), resultDTO.getFile(), resultDTO.isEditable());
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                Position lineStart = (Position)data;
                EditorScrollPane editorScrollPane = tabPaneBuilderUI.getScrollPaneFromActiveTab();
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
                        openFile(content, openedFile.toFile(), true);
                        applicatonState.addCurrentFileToClassesToRecompile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case AUTOSAVE_DONE:
                EditorScrollPane scrollPane = tabPaneBuilderUI.getScrollPaneFromActiveTab();
                SyntaxColorStyledDocument syntaxColorDocument = (SyntaxColorStyledDocument) scrollPane.getTextEditor().getDocument();
                syntaxColorDocument.checkForTextChanges();
                break;

        }
    }

    private void openFile(List<String> lines, File file, boolean editable) {
        String text = String.join(System.lineSeparator(), lines);
        if (tabPaneBuilderUI.containsTab(file)){
            tabPaneBuilderUI.selectTab(file);
        }
        else{
            JScrollPane scrollPane = createScrollableTextEditor(text, editable);
            tabPaneBuilderUI.addTab(scrollPane, file, lines);

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
