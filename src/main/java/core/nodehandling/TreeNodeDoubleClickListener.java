package core.nodehandling;

import core.backend.FileAutoSaver;
import core.backend.ProjectNodeOpener;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.dto.TreeNodeFileDTO;
import core.uibuilders.ProjectStructureNodesHandler;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Component
public class TreeNodeDoubleClickListener extends MouseAdapter {


    private UIEventsQueue uiEventsQueue;

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private FileAutoSaver fileAutoSaver;

    private ProjectNodeOpener projectNodeOpener;

    private ApplicatonState applicatonState;

    public TreeNodeDoubleClickListener(UIEventsQueue uiEventsQueue, ProjectStructureNodesHandler projectStructureNodesHandler, FileAutoSaver fileAutoSaver, ProjectNodeOpener projectNodeOpener, ApplicatonState applicatonState) {
        this.uiEventsQueue = uiEventsQueue;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.fileAutoSaver = fileAutoSaver;
        this.projectNodeOpener = projectNodeOpener;
        this.applicatonState = applicatonState;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = projectStructureNodesHandler.getContext(e);
            List<TreeNodeFileDTO[]> nodeNames = context.getNodesPaths();
            FileReadResultDTO resultDTO = projectNodeOpener.openNode(nodeNames.get(0));
            if (resultDTO.isReaded()){
                fileAutoSaver.save();
                applicatonState.setOpenedFile(resultDTO.getFile());
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
