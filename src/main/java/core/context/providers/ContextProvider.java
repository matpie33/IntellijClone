package core.context.providers;

import core.contextMenu.ContextType;

import java.awt.event.MouseEvent;

public interface ContextProvider {
    Object getContext(MouseEvent e);

    ContextType getContextType();
}
