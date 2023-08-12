package root.core.mouselisteners;

import root.core.context.ContextConfiguration;
import root.core.context.contextMenu.ContextType;
import root.core.dto.MenuItemDTO;
import root.ui.components.ContextMenu;

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
