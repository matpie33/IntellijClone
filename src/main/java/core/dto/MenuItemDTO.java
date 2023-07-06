package core.dto;

import core.context.actionlisteners.ContextActionListener;

public class MenuItemDTO {

    private String menuValue;

    private ContextActionListener actionListener;

    public MenuItemDTO(String menuValue, ContextActionListener actionListener) {
        this.menuValue = menuValue;
        this.actionListener = actionListener;
    }

    public String getMenuValue() {
        return menuValue;
    }

    public ContextActionListener getActionListener() {
        return actionListener;
    }
}
