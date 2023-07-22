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


    private JPanel panel;

    private ContextConfiguration contextConfiguration;

    private JTextPane editorText;


    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    private Font editorFont = new Font("DejaVu Sans Mono", Font.PLAIN, FontsConstants.FONT_SIZE);

    private SyntaxColorStyledDocument syntaxColoringDocument;

    private FileIO fileIO;

    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState, SyntaxColorStyledDocument syntaxColoringDocument, FileIO fileIO) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
        this.syntaxColoringDocument = syntaxColoringDocument;
        this.fileIO = fileIO;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        editorText = new JTextPane(syntaxColoringDocument);
        editorText.setCaret(new ImprovedCaret());
        editorText.getCaret().setBlinkRate(500);
        syntaxColoringDocument.initialize(editorFont, editorText);
        JScrollPane editorScrollPane = new JScrollPane(editorText);
        editorText.setFont(editorFont);
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
                applicatonState.addCurrentFileToClassesToRecompile();
            }
        });
        editorText.addMouseListener(new PopupMenuRequestListener(ContextType.FILE_EDITOR, contextConfiguration));
        panel.add(editorScrollPane, BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return panel;
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
