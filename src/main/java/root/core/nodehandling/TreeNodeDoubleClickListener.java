package root.core.nodehandling;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.dto.ApplicationState;
import root.core.dto.FileReadResultDTO;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.fileio.FileAutoSaver;
import root.core.fileio.ProjectFileOpener;
import root.core.ui.tree.ProjectStructureNode;
import root.core.ui.tree.ProjectStructureNodeType;
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

    private ApplicationState applicationState;

    public TreeNodeDoubleClickListener(UIEventsQueue uiEventsQueue, ProjectStructureNodesHandler projectStructureNodesHandler, FileAutoSaver fileAutoSaver, ProjectFileOpener projectFileOpener, ApplicationState applicationState) {
        this.uiEventsQueue = uiEventsQueue;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.fileAutoSaver = fileAutoSaver;
        this.projectFileOpener = projectFileOpener;
        this.applicationState = applicationState;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {

            ProjectStructureSelectionContextDTO context = projectStructureNodesHandler.getContext(e);
            List<ProjectStructureNode[]> nodeNames = context.getNodesPaths();
            ProjectStructureNode[] firstSelectionPath = nodeNames.get(0);
            ProjectStructureNode lastNode = firstSelectionPath[firstSelectionPath.length - 1];
            ClassOrigin classOrigin = lastNode.getClassOrigin();
            ProjectStructureNodeType nodeType = lastNode.getProjectStructureNodeType();
            if (nodeType.equals(ProjectStructureNodeType.EMPTY) || nodeType.equals(ProjectStructureNodeType.DIRECTORY)){
                return;
            }
            FileReadResultDTO resultDTO = projectFileOpener.openNode(firstSelectionPath, classOrigin);
            if (resultDTO.isReadSuccessfully()){
                fileAutoSaver.save();
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, resultDTO);
            }
        }
    }


}
