package core.panelbuilders;

import com.github.javaparser.Position;
import core.backend.FileAutoSaver;
import core.constants.FontsConstants;
import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.ui.components.SyntaxColorStyledDocument;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;

@Component
public class FileEditorPanelBuilder implements UIEventObserver {


    private JPanel panel;

    private ContextConfiguration contextConfiguration;

    private JTextPane editorText;


    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    private Font editorFont = new Font("DejaVu Sans Mono", Font.PLAIN, FontsConstants.FONT_SIZE);

    private SyntaxColorStyledDocument syntaxColoringDocument = new SyntaxColorStyledDocument();

    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        editorText = new JTextPane(syntaxColoringDocument);
        editorText.setCaret(new ImprovedCaret());
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
                editorText.setText(String.join(System.lineSeparator(), resultDTO.getLines()));
                break;
            case CLASS_STRUCTURE_NODE_CLICKED:
                try {
                    Position lineStart = (Position)data;
                    Element element = editorText.getDocument().getDefaultRootElement();
                    editorText.setCaretPosition(element.getElement(lineStart.line - 1).getStartOffset() + lineStart.column-1);
                    editorText.requestFocus();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                break;
        }
    }
}
