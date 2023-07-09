package core.dto;

import core.context.actionlisteners.ContextAction;
import core.context.conditionalmenu.ConditionChecker;

public class MenuItemDTO {

    private ConditionChecker conditionChecker;

    private String menuValue;

    private ContextAction actionListener;

    public MenuItemDTO(String menuValue, ContextAction actionListener) {
        this.menuValue = menuValue;
        this.actionListener = actionListener;
    }

    public MenuItemDTO( String menuValue, ContextAction actionListener, ConditionChecker conditionChecker) {
        this.conditionChecker = conditionChecker;
        this.menuValue = menuValue;
        this.actionListener = actionListener;
    }

    public ConditionChecker getConditionChecker() {
        return conditionChecker;
    }


    public String getMenuValue() {
        return menuValue;
    }

    public ContextAction getAction() {
        return actionListener;
    }
}
