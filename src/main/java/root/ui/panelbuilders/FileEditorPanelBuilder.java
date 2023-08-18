package root.ui.panelbuilders;

import com.github.javaparser.Position;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.classmanipulating.ClassStructureParser;
import root.core.codecompletion.CodeCompletionNavigator;
import root.core.constants.FontsConstants;
import root.core.context.ContextConfiguration;
import root.core.context.contextMenu.ContextType;
import root.core.dto.ApplicationState;
import root.core.dto.ClassStructureDTO;
import root.core.dto.FileReadResultDTO;
import root.core.dto.FileSystemChangeDTO;
import root.core.fileio.FileAutoSaver;
import root.core.fileio.FileIO;
import root.core.mouselisteners.PopupMenuRequestListener;
import root.core.shortcuts.FileEditorShortcuts;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.ui.components.*;
import root.ui.uibuilders.TabPaneBuilderUI;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class FileEditorPanelBuilder implements UIEventObserver, ApplicationContextAware {


    private ContextConfiguration contextConfiguration;

    private FileAutoSaver fileAutoSaver;

    private ApplicationState applicationState;

    private Font editorFont = new Font("DejaVu Sans Mono", Font.PLAIN, FontsConstants.FONT_SIZE);


    private FileIO fileIO;

    private JPanel rootPanel;
    private TabPaneBuilderUI tabPaneBuilderUI;
    private ApplicationContext applicationContext;

    private FileEditorShortcuts fileEditorShortcuts;

    private CodeCompletionPopup codeCompletionPopup;

    private CodeCompletionNavigator codeCompletionNavigator;

    private ClassStructureParser classStructureParser;


    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicationState applicationState, FileIO fileIO, TabPaneBuilderUI tabPaneBuilderUI, FileEditorShortcuts fileEditorShortcuts, CodeCompletionPopup codeCompletionPopup, CodeCompletionNavigator codeCompletionNavigator, ClassStructureParser classStructureParser) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicationState = applicationState;
        this.fileIO = fileIO;
        this.tabPaneBuilderUI = tabPaneBuilderUI;
        this.fileEditorShortcuts = fileEditorShortcuts;
        this.codeCompletionPopup = codeCompletionPopup;
        this.codeCompletionNavigator = codeCompletionNavigator;
        this.classStructureParser = classStructureParser;
    }

    @PostConstruct
    public void init() {
        rootPanel = new JPanel(new BorderLayout());

        EditorScrollPane scrollPane = createScrollableTextEditor("", true);
        tabPaneBuilderUI.addTab(scrollPane, new File("untitled.java"), ClassOrigin.SOURCES);
        JTabbedPane tabbedPane = tabPaneBuilderUI.getTabbedPane();
        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        fileEditorShortcuts.assignShortcuts(tabbedPane);
    }

    private EditorScrollPane createScrollableTextEditor(String text, boolean editable) {
        SyntaxColorStyledDocument document = applicationContext.getBean(SyntaxColorStyledDocument.class);
        FileEditorComponent editorText = new FileEditorComponent(document);
        editorText.setEditable(editable);
        editorText.setCaret(new ImprovedCaret());
        editorText.getCaret().setBlinkRate(500);
        editorText.setFont(editorFont);
        editorText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
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
                FileReadResultDTO resultDTO = (FileReadResultDTO) data;
                applicationState.setOpenedFile(resultDTO.getFile());
                try {
                    openFile(resultDTO.getContentLines(), resultDTO.getFile(), resultDTO.getClassOrigin());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                Position position = (Position) data;
                EditorScrollPane editorScrollPane = tabPaneBuilderUI.getScrollPaneFromActiveTab();
                JTextPane editorText = editorScrollPane.getTextEditor();
                scrollTextPaneToPosition(editorText, position);
                break;
            case PROJECT_STRUCTURE_CHANGED:
                if (applicationState.getOpenedFile() == null) {
                    return;
                }
                FileSystemChangeDTO fileSystemChange = (FileSystemChangeDTO) data;
                List<Path> modifiedFiles = fileSystemChange.getModifiedFiles();
                Path openedFile = applicationState.getOpenedFile().toPath();
                if (modifiedFiles.contains(openedFile)) {
                    try {
                        List<String> content = fileIO.getContent(openedFile);
                        openFile(content, openedFile.toFile(), ClassOrigin.SOURCES);
                        applicationState.addCurrentFileToClassesToRecompile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case AUTOSAVE_DONE:
                EditorScrollPane scrollPane = tabPaneBuilderUI.getScrollPaneFromActiveTab();
                SyntaxColorStyledDocument syntaxColorDocument = (SyntaxColorStyledDocument) scrollPane.getTextEditor().getDocument();
                syntaxColorDocument.checkForTextChanges();
                syntaxColorDocument.doWordsColoring();
                break;

        }
    }

    private void scrollTextPaneToPosition(JTextPane editorText, Position position) {


        Element rootElement = editorText.getDocument().getDefaultRootElement();
        Element elementForGivenLine = rootElement.getElement(position.line - 1);
        editorText.requestFocusInWindow();
        SwingUtilities.invokeLater(() -> {
            try {
                Rectangle2D rectangleForDestinationLine = editorText.modelToView2D(elementForGivenLine.getStartOffset() + position.column - 1);
                int viewportHeight = editorText.getParent().getHeight();
                Rectangle visibleRectangular = editorText.getVisibleRect();
                Rectangle destinationRectangular = new Rectangle((int) rectangleForDestinationLine.getX(), (int) rectangleForDestinationLine.getY(), (int) rectangleForDestinationLine.getWidth(), (int) rectangleForDestinationLine.getHeight());
                int destinationYPosition = destinationRectangular.y;
                if (destinationIsBelowVisibleRectangular(visibleRectangular, destinationRectangular)) {
                    destinationYPosition += 4 * viewportHeight / 5;
                } else {
                    destinationYPosition -= viewportHeight / 5;
                }
                destinationRectangular.y = destinationYPosition;
                editorText.scrollRectToVisible(destinationRectangular);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private boolean destinationIsBelowVisibleRectangular(Rectangle visibleRectangular, Rectangle destinationRectangular) {
        return visibleRectangular.y + visibleRectangular.height < destinationRectangular.y;
    }

    private void openFile(List<String> lines, File file, ClassOrigin classOrigin) throws FileNotFoundException {
        String text = String.join(System.lineSeparator(), lines);
        ClassStructureDTO classStructure = applicationState.getClassStructureOfOpenedFile();
        if (classStructure == null && classOrigin.isSourceFile()){
            classStructureParser.parseClassStructure(file, ClassOrigin.SOURCES);
            classStructure = applicationState.getClassStructureOfOpenedFile();
        }
        Position classDeclarationPosition = classStructure == null? new Position(1,1): classStructure.getClassDeclarationPosition();
        if (tabPaneBuilderUI.containsTab(file)){
            tabPaneBuilderUI.selectTab(file);
        }
        else{
            EditorScrollPane editorScrollPane = createScrollableTextEditor(text, classOrigin.isEditable());
            tabPaneBuilderUI.addTab(editorScrollPane, file, classOrigin);
            scrollTextPaneToPosition(editorScrollPane.getTextEditor(), classDeclarationPosition);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
