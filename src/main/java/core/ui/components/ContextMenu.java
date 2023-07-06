package core.ui.components;

import core.context.actionlisteners.ContextActionListener;
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
                ContextActionListener actionListener = menu.getActionListener();
                actionListener.setContext(context);
                menuItem.addActionListener(actionListener);
                add(menuItem);
            }

        }
    }
}
