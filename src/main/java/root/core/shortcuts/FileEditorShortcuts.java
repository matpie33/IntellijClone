package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.dto.ShortcutDTO;
import root.core.undoredo.RedoAction;
import root.core.undoredo.UndoAction;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileEditorShortcuts {

    private List<ShortcutDTO> shortcuts = new ArrayList<>();

    private ShortcutAssigner shortcutAssigner;

    private UndoAction undoAction;
    private RedoAction redoAction;

    public FileEditorShortcuts(UndoAction undoAction, RedoAction redoAction, ShortcutAssigner shortcutAssigner) {
        this.shortcutAssigner = shortcutAssigner;
        this.undoAction = undoAction;
        this.redoAction = redoAction;
    }

    @PostConstruct
    public void init (){
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo", undoAction));
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo", redoAction));
    }


    public void assignShortcuts(JComponent component){
        shortcuts.forEach(shortcut -> shortcutAssigner.assignShortcutOnFocusedWindow(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }


}
