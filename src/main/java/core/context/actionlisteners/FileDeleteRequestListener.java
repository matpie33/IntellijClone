package core.context.actionlisteners;

import core.Main;
import core.backend.FileIO;
import core.constants.DialogText;
import core.dto.ProjectStructureSelectionContextDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

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
        List<String[]> nodesPaths = context.getNodesPaths();
        if (nodesPaths.isEmpty()){
            return;
        }
        String objectToDelete = getObjectToDeleteName(nodesPaths);
        int result = JOptionPane.showConfirmDialog(Main.FRAME.getContentPane(),
                String.format(DialogText.CONFIRM_FILE_DELETE, objectToDelete));
        if (result == JOptionPane.YES_OPTION) {
            for (String[] nodeNames : nodesPaths) {
                fileIO.removeFile(nodeNames);
            }
            uiEventsQueue.dispatchEvent(UIEventType.FILE_REMOVED_FROM_PROJECT, context);
        }
    }

    private String getObjectToDeleteName(List<String[]> nodesPaths) {
        String objectToDelete;
        if (nodesPaths.size()>1){
            objectToDelete = String.format("%d files", nodesPaths.size());
        }
        else{
            String[] paths = nodesPaths.iterator().next();
            objectToDelete = paths[paths.length-1];

        }
        return objectToDelete;
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
