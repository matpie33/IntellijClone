package root.core.context.providers;

import root.core.context.contextMenu.ContextType;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public interface ContextProvider<T> {
    T getContext(MouseEvent e);

    T getContext(ActionEvent actionEvent);

    ContextType getContextType();
}
