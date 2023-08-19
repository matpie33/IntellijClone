package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.dto.ShortcutDTO;
import root.core.textactions.DeleteLineAction;
import root.core.textactions.DuplicateLinesAction;
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

    private DuplicateLinesAction duplicateLinesAction;
    private DeleteLineAction deleteLineAction;

    public FileEditorShortcuts(UndoAction undoAction, RedoAction redoAction, ShortcutAssigner shortcutAssigner, DuplicateLinesAction duplicateLinesAction, DeleteLineAction deleteLineAction) {
        this.shortcutAssigner = shortcutAssigner;
        this.undoAction = undoAction;
        this.redoAction = redoAction;
        this.duplicateLinesAction = duplicateLinesAction;
        this.deleteLineAction = deleteLineAction;
    }

    @PostConstruct
    public void init (){
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo", undoAction));
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo", redoAction));
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), "delete line", deleteLineAction));
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK|KeyEvent.ALT_DOWN_MASK), "duplicateLine", duplicateLinesAction));
    }


    public void assignShortcuts(JComponent component){
        shortcuts.forEach(shortcut -> shortcutAssigner.assignShortcutOnFocusedWindow(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }


}
