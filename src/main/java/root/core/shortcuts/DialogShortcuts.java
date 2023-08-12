package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.context.actionlisteners.AcceptDialogValuesListener;
import root.core.context.actionlisteners.CloseDialogListener;
import root.core.dto.ShortcutDTO;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

@Component
public class DialogShortcuts {

    private  List<ShortcutDTO> rootPaneShortcuts = new ArrayList<>();

    private CloseDialogListener closeDialogListener;

    private AcceptDialogValuesListener acceptDialogValuesListener;

    private ShortcutAssigner shortcutAssigner;

    public DialogShortcuts( CloseDialogListener closeDialogListener, AcceptDialogValuesListener acceptDialogValuesListener, ShortcutAssigner shortcutAssigner) {
        this.closeDialogListener = closeDialogListener;
        this.acceptDialogValuesListener = acceptDialogValuesListener;
        this.shortcutAssigner = shortcutAssigner;
    }

    @PostConstruct
    public void init (){
        rootPaneShortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "accept",acceptDialogValuesListener ));
        rootPaneShortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close",closeDialogListener ));
    }


    public void assignShortcuts(JComponent component){
        rootPaneShortcuts.forEach(shortcut -> shortcutAssigner.assignShortcutOnFocusedWindow(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }


}
