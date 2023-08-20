package root.ui.components;

import root.core.context.actionlisteners.ContextAction;
import root.core.dto.MenuItemDTO;

import javax.swing.*;
import java.util.List;

public class ContextMenu extends JPopupMenu {


    public ContextMenu(List<MenuItemDTO> menus, Object context) {
        for (MenuItemDTO menu : menus) {
            if (menu.getMenuValue().equals("-")){
                add (new JSeparator());
            }
            else{

                if (menu.getConditionChecker() != null && !menu.getConditionChecker().isConditionFulfilled(context)){
                    continue;
                }

                List<MenuItemDTO> subMenus = menu.getSubMenus();
                boolean hasSubmenus = !subMenus.isEmpty();
                JMenuItem menuItem = createMenuItem(context, menu, hasSubmenus);
                for (MenuItemDTO subMenu : subMenus) {
                    boolean subMenuHasSubmenus = !subMenu.getSubMenus().isEmpty();
                    JMenuItem subMenuItem = createMenuItem(context, subMenu, subMenuHasSubmenus);
                    menuItem.add(subMenuItem);
                }
                add(menuItem);
            }

        }
    }

    private JMenuItem createMenuItem(Object context, MenuItemDTO menuDTO, boolean hasSubmenus) {
        JMenuItem menuItem = hasSubmenus? new JMenu(menuDTO.getMenuValue()): new JMenuItem(menuDTO.getMenuValue());
        ContextAction action = menuDTO.getAction();
        action.setContext(context);
        menuItem.addActionListener(action);
        return menuItem;
    }
}
