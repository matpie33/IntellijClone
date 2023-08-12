package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicatonState;
import root.core.dto.FileReadResultDTO;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.TreeNodeFileDTO;
import root.core.fileio.FileAutoSaver;
import root.core.fileio.ProjectFileOpener;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Component
public class TreeNodeDoubleClickListener extends MouseAdapter {


    private UIEventsQueue uiEventsQueue;

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private FileAutoSaver fileAutoSaver;

    private ProjectFileOpener projectFileOpener;

    private ApplicatonState applicatonState;

    public TreeNodeDoubleClickListener(UIEventsQueue uiEventsQueue, ProjectStructureNodesHandler projectStructureNodesHandler, FileAutoSaver fileAutoSaver, ProjectFileOpener projectFileOpener, ApplicatonState applicatonState) {
        this.uiEventsQueue = uiEventsQueue;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.fileAutoSaver = fileAutoSaver;
        this.projectFileOpener = projectFileOpener;
        this.applicatonState = applicatonState;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = projectStructureNodesHandler.getContext(e);
            List<TreeNodeFileDTO[]> nodeNames = context.getNodesPaths();
            FileReadResultDTO resultDTO = projectFileOpener.openNode(nodeNames.get(0));
            if (resultDTO.isReaded()){
                fileAutoSaver.save();
                applicatonState.setOpenedFile(resultDTO.getFile());
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
