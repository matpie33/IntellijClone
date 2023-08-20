package root.core.dto;

import root.core.context.actionlisteners.ContextAction;
import root.core.context.conditionalmenu.ConditionChecker;

import java.util.ArrayList;
import java.util.List;

public class MenuItemDTO {

    private ConditionChecker conditionChecker;

    private String menuValue;

    private List<MenuItemDTO> subMenus = new ArrayList<>();

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

    public List<MenuItemDTO> getSubMenus() {
        return subMenus;
    }

    public MenuItemDTO addSubMenu (MenuItemDTO menuItemDTO){
        subMenus.add(menuItemDTO);
        return this;
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
