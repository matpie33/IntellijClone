package core.panelbuilders;

import com.github.javaparser.Position;
import core.backend.FileAutoSaver;
import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.dto.FileReadResultDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@Component
public class FileEditorPanelBuilder implements UIEventObserver {


    private JPanel panel;

    private ContextMenuValues contextMenuValues;

    private JTextArea editorText;


    private FileAutoSaver fileAutoSaver;

    public FileEditorPanelBuilder(ContextMenuValues contextMenuValues, FileAutoSaver fileAutoSaver) {
        this.contextMenuValues = contextMenuValues;
        this.fileAutoSaver = fileAutoSaver;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        editorText = new JTextArea("code here");
        editorText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fileAutoSaver.recordKeyRelease(editorText.getText());
            }
        });
        editorText.addMouseListener(new PopupMenuRequestListener(ContextType.FILE_EDITOR, contextMenuValues));
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
