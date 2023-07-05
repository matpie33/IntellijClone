package core.mouselisteners;

import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.ui.components.ContextMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PopupMenuRequestListener extends MouseAdapter {

    private ContextType contextType;

    private ContextMenuValues contextMenuValues;

    public PopupMenuRequestListener(ContextType contextType, ContextMenuValues contextMenuValues) {
        this.contextType = contextType;
        this.contextMenuValues = contextMenuValues;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            List<String> values = contextMenuValues.getValues(contextType);
            ContextMenu contextMenu = new ContextMenu(values);
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
