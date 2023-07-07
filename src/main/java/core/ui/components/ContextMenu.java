package core.ui.components;

import core.context.actionlisteners.ContextAction;
import core.dto.MenuItemDTO;

import javax.swing.*;
import java.util.List;

public class ContextMenu extends JPopupMenu {


    public ContextMenu(List<MenuItemDTO> menus, Object context) {
        for (MenuItemDTO menu : menus) {
            if (menu.getMenuValue().equals("-")){
                add (new JSeparator());
            }
            else{

                JMenuItem menuItem = new JMenuItem(menu.getMenuValue());
                ContextAction action = menu.getAction();
                action.setContext(context);
                menuItem.addActionListener(action);
                add(menuItem);
            }

        }
    }
}
