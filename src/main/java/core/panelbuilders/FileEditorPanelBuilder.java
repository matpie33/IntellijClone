package core.panelbuilders;

import com.github.javaparser.Position;
import core.backend.FileAutoSaver;
import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@Component
public class FileEditorPanelBuilder implements UIEventObserver {


    private JPanel panel;

    private ContextConfiguration contextConfiguration;

    private JTextArea editorText;


    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    public FileEditorPanelBuilder(ContextConfiguration contextConfiguration, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState) {
        this.fileAutoSaver = fileAutoSaver;
        this.contextConfiguration = contextConfiguration;
        this.applicatonState = applicatonState;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        editorText = new JTextArea("code here");
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
                applicatonState.addCurrentFileToClassesToRecompile();
            }
        });
        editorText.addMouseListener(new PopupMenuRequestListener(ContextType.FILE_EDITOR, contextConfiguration));
        panel.add(editorText, BorderLayout.CENTER);
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
                    editorText.setCaretPosition(editorText.getLineStartOffset(lineStart.line - 1) + lineStart.column-1);
                    editorText.requestFocus();
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                break;
        }
    }
}
