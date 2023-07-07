package core.dto;

import core.context.actionlisteners.ContextAction;

public class MenuItemDTO {

    private String menuValue;

    private ContextAction actionListener;

    public MenuItemDTO(String menuValue, ContextAction actionListener) {
        this.menuValue = menuValue;
        this.actionListener = actionListener;
    }

    public String getMenuValue() {
        return menuValue;
    }

    public ContextAction getAction() {
        return actionListener;
    }
}
