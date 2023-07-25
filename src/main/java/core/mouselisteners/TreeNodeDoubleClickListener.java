package core.mouselisteners;

import core.backend.FileAutoSaver;
import core.backend.FileIO;
import core.backend.ProjectNodeOpener;
import core.context.providers.NodePathManipulation;
import core.dto.FileReadResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.dto.TreeNodeFileDTO;
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

    private ProjectNodeOpener projectNodeOpener;

    public TreeNodeDoubleClickListener(FileIO fileIO, UIEventsQueue uiEventsQueue, NodePathManipulation nodePathManipulation, FileAutoSaver fileAutoSaver, ProjectNodeOpener projectNodeOpener) {
        this.fileIO = fileIO;
        this.uiEventsQueue = uiEventsQueue;
        this.nodePathManipulation = nodePathManipulation;
        this.fileAutoSaver = fileAutoSaver;
        this.projectNodeOpener = projectNodeOpener;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = nodePathManipulation.getContext(e);
            List<TreeNodeFileDTO[]> nodeNames = context.getNodesPaths();
            FileReadResultDTO resultDTO = projectNodeOpener.openNode(nodeNames.get(0));
            if (resultDTO.isReaded()){
                fileAutoSaver.save();
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
