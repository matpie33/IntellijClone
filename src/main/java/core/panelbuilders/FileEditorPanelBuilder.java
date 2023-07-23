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
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class FileEditorPanelBuilder implements UIEventObserver {


    private ContextConfiguration contextConfiguration;

    private JTextPane editorText;


    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    private Font editorFont = new Font("DejaVu Sans Mono", Font.PLAIN, FontsConstants.FONT_SIZE);

    private SyntaxColorStyledDocument syntaxColoringDocument;

    private FileIO fileIO;
    private EditorScrollPane editorScrollPane;

    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState, SyntaxColorStyledDocument syntaxColoringDocument, FileIO fileIO) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
        this.syntaxColoringDocument = syntaxColoringDocument;
        this.fileIO = fileIO;
    }

    @PostConstruct
    public void init (){
        JPanel panel = new JPanel(new BorderLayout());
        editorText = new JTextPane(syntaxColoringDocument) {
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

        syntaxColoringDocument.initialize(editorFont, editorText);
        editorScrollPane = new EditorScrollPane(editorText);
        editorScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        editorScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        panel.add(editorScrollPane, BorderLayout.CENTER);
    }

    public JScrollPane getRootScrollPane() {
        return editorScrollPane;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType) {
            case FILE_OPENED_FOR_EDIT:
                @SuppressWarnings("unchecked")
                FileReadResultDTO resultDTO = (FileReadResultDTO)data;
                setFileContent(resultDTO.getLines());
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                Position lineStart = (Position)data;
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
                        setFileContent(content);
                        applicatonState.addCurrentFileToClassesToRecompile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    }

    private void setFileContent(List<String> lines) {
        editorText.setText(String.join(System.lineSeparator(), lines));
    }
}
