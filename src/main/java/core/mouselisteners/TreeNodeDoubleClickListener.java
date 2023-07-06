package core.mouselisteners;

import core.backend.FileIO;
import core.context.providers.NodePathExtractor;
import core.dto.FileReadResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class TreeNodeDoubleClickListener extends MouseAdapter {

    private FileIO fileIO;

    private UIEventsQueue uiEventsQueue;

    private NodePathExtractor nodePathExtractor;

    public TreeNodeDoubleClickListener(FileIO fileIO, UIEventsQueue uiEventsQueue, NodePathExtractor nodePathExtractor) {
        this.fileIO = fileIO;
        this.uiEventsQueue = uiEventsQueue;
        this.nodePathExtractor = nodePathExtractor;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = (ProjectStructureSelectionContextDTO) nodePathExtractor.getContext(e);
            FileReadResultDTO resultDTO = fileIO.read(context.getNodeNames());
            if (resultDTO.isReaded()){
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
