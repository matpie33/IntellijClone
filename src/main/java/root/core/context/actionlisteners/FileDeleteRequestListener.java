package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.constants.DialogText;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.ProjectStructureTreeElementDTO;
import root.core.fileio.FileIO;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

@Component
public class FileDeleteRequestListener extends ContextAction<ProjectStructureSelectionContextDTO> {

    private FileIO fileIO;

    private ProjectStructureSelectionContextDTO context;

    private UIEventsQueue uiEventsQueue;

    public FileDeleteRequestListener(FileIO fileIO, UIEventsQueue uiEventsQueue) {
        this.fileIO = fileIO;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectStructureSelectionContextDTO context = this.context;
        List<ProjectStructureTreeElementDTO[]> nodesPaths = context.getNodesPaths();
        if (nodesPaths.isEmpty()){
            return;
        }
        String objectToDelete = getObjectToDeleteName(nodesPaths);
        int result = JOptionPane.showConfirmDialog(Main.FRAME.getContentPane(),
                String.format(DialogText.CONFIRM_FILE_DELETE, objectToDelete));
        if (result == JOptionPane.YES_OPTION) {
            for (ProjectStructureTreeElementDTO[] nodeNames : nodesPaths) {
                fileIO.removeFile(nodeNames);
            }
            uiEventsQueue.dispatchEvent(UIEventType.FILE_REMOVED_FROM_PROJECT, context);
        }
    }

    private String getObjectToDeleteName(List<ProjectStructureTreeElementDTO[]> nodesPaths) {
        String objectToDelete;
        if (nodesPaths.size()>1){
            objectToDelete = String.format("%d files", nodesPaths.size());
        }
        else{
            ProjectStructureTreeElementDTO[] paths = nodesPaths.iterator().next();
            objectToDelete = paths[paths.length-1].getDisplayName();

        }
        return objectToDelete;
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
