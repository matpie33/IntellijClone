package core.mouselisteners;

import core.backend.FileAutoSaver;
import core.backend.FileIO;
import core.context.providers.NodePathManipulation;
import core.dto.FileReadResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Component
public class TreeNodeDoubleClickListener extends MouseAdapter {

    private FileIO fileIO;

    private UIEventsQueue uiEventsQueue;

    private NodePathManipulation nodePathManipulation;

    private FileAutoSaver fileAutoSaver;

    public TreeNodeDoubleClickListener(FileIO fileIO, UIEventsQueue uiEventsQueue, NodePathManipulation nodePathManipulation, FileAutoSaver fileAutoSaver) {
        this.fileIO = fileIO;
        this.uiEventsQueue = uiEventsQueue;
        this.nodePathManipulation = nodePathManipulation;
        this.fileAutoSaver = fileAutoSaver;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = nodePathManipulation.getContext(e);
            List<String[]> nodeNames = context.getNodesPaths();
            fileAutoSaver.save();
            FileReadResultDTO resultDTO = fileIO.read(nodeNames.iterator().next());
            if (resultDTO.isReaded()){
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
