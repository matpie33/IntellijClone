package core.context.actionlisteners;

import core.context.providers.NodePathManipulation;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class FileDeleteKeyPressListener extends AbstractAction {

    private FileDeleteRequestListener fileDeleteRequestListener;

    private NodePathManipulation nodePathManipulation;

    public FileDeleteKeyPressListener(FileDeleteRequestListener fileDeleteRequestListener, NodePathManipulation nodePathManipulation) {
        this.fileDeleteRequestListener = fileDeleteRequestListener;
        this.nodePathManipulation = nodePathManipulation;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectStructureSelectionContextDTO context = nodePathManipulation.getContext(e);
        fileDeleteRequestListener.setContext(context);
        fileDeleteRequestListener.actionPerformed(e);
    }
}
