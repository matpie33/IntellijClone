package core.dto;

import javax.swing.*;

public class ShortcutDTO {
    private KeyStroke keyStroke;
    private String actionName;
    private AbstractAction action;

    private boolean inFocusedWindow;

    public ShortcutDTO(KeyStroke keyStroke, String actionName, AbstractAction action) {
        this.keyStroke = keyStroke;
        this.actionName = actionName;
        this.action = action;
    }

    public boolean isInFocusedWindow() {
        return inFocusedWindow;
    }

    public void setInFocusedWindow(boolean inFocusedWindow) {
        this.inFocusedWindow = inFocusedWindow;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public String getActionName() {
        return actionName;
    }

    public AbstractAction getAction() {
        return action;
    }
}
