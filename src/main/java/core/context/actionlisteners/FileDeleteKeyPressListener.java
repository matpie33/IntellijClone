package core.context.actionlisteners;

import core.context.providers.NodePathExtractor;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class FileDeleteKeyPressListener extends AbstractAction {

    private FileDeleteRequestListener fileDeleteRequestListener;

    private NodePathExtractor nodePathExtractor;

    public FileDeleteKeyPressListener(FileDeleteRequestListener fileDeleteRequestListener, NodePathExtractor nodePathExtractor) {
        this.fileDeleteRequestListener = fileDeleteRequestListener;
        this.nodePathExtractor = nodePathExtractor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectStructureSelectionContextDTO context = nodePathExtractor.getContextFromKeyPress(e);
        fileDeleteRequestListener.setContext(context);
        fileDeleteRequestListener.actionPerformed(e);
    }
}
