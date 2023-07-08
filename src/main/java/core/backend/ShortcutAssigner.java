package core.backend;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.KeyEvent;

@Component
public class ShortcutAssigner {

    public void assignShortcut (JComponent component, KeyStroke keyStroke, String actionName, AbstractAction action){
        component.getInputMap().put(keyStroke, actionName);
        component.getActionMap().put(actionName, action);
    }

    public void assignShortcutOnFocusedWindow (JComponent component, KeyStroke keyStroke, String actionName, AbstractAction action){
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionName);
        component.getActionMap().put(actionName, action);
    }

}
