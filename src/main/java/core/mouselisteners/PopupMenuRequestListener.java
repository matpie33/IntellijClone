package core.mouselisteners;

import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.MenuItemDTO;
import core.ui.components.ContextMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PopupMenuRequestListener extends MouseAdapter {

    private ContextType contextType;


    private ContextConfiguration contextConfiguration;

    public PopupMenuRequestListener(ContextType contextType, ContextConfiguration contextConfiguration) {
        this.contextType = contextType;
        this.contextConfiguration = contextConfiguration;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            List<MenuItemDTO> values = contextConfiguration.getContextMenuValues(contextType);
            Object context = contextConfiguration.getContextProvider(contextType).getContext(e);
            ContextMenu contextMenu = new ContextMenu(values, context);
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
