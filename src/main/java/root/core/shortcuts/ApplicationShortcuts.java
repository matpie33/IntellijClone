package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.dto.ShortcutDTO;
import root.core.menuitemlisteners.SettingsMenuItemListener;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationShortcuts {

    private List<ShortcutDTO> shortcuts = new ArrayList<>();

    private ShortcutAssigner shortcutAssigner;

    private SettingsMenuItemListener settingsMenuItemListener;

    public ApplicationShortcuts(ShortcutAssigner shortcutAssigner, SettingsMenuItemListener settingsMenuItemListener) {
        this.shortcutAssigner = shortcutAssigner;
        this.settingsMenuItemListener = settingsMenuItemListener;
    }

    @PostConstruct
    public void init (){
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), "openSettings",settingsMenuItemListener ));
    }


    public void assignShortcuts(JComponent component){
        shortcuts.forEach(shortcut -> shortcutAssigner.assignShortcutOnFocusedWindow(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }


}
