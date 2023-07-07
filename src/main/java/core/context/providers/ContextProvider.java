package core.context.providers;

import core.contextMenu.ContextType;
import core.dto.ProjectStructureSelectionContextDTO;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public interface ContextProvider<T> {
    T getContext(MouseEvent e);

    T getContext(ActionEvent actionEvent);

    ContextType getContextType();
}
