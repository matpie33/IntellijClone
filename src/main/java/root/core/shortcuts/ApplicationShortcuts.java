package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.dto.ShortcutDTO;
import root.core.menuitemlisteners.NavigateToClassMenuItemListener;
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
    private NavigateToClassMenuItemListener navigateToClassMenuItemListener;

    public ApplicationShortcuts(ShortcutAssigner shortcutAssigner, SettingsMenuItemListener settingsMenuItemListener, NavigateToClassMenuItemListener navigateToClassMenuItemListener) {
        this.shortcutAssigner = shortcutAssigner;
        this.settingsMenuItemListener = settingsMenuItemListener;
        this.navigateToClassMenuItemListener = navigateToClassMenuItemListener;
    }

    @PostConstruct
    public void init (){
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), "openSettings",settingsMenuItemListener ));
        shortcuts.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "navigateToClass", navigateToClassMenuItemListener));
    }


    public void assignShortcuts(JComponent component){
        shortcuts.forEach(shortcut -> shortcutAssigner.assignShortcutOnFocusedWindow(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }


}
