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

@Component
public class FileDeleteListener implements ContextActionListener {

    private FileIO fileIO;

    private Object context;

    private UIEventsQueue uiEventsQueue;

    public FileDeleteListener(FileIO fileIO, UIEventsQueue uiEventsQueue) {
        this.fileIO = fileIO;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectStructureSelectionContextDTO context = (ProjectStructureSelectionContextDTO) this.context;
        String[] nodeNames = context.getNodeNames();
        int result = JOptionPane.showConfirmDialog(Main.FRAME.getContentPane(),
                String.format(DialogText.CONFIRM_FILE_DELETE, nodeNames[nodeNames.length-1]));
        if (result == JOptionPane.YES_OPTION){
            fileIO.removeFile(nodeNames);
            uiEventsQueue.dispatchEvent(UIEventType.FILE_REMOVED_FROM_PROJECT, context);
        }
    }

    @Override
    public void setContext(Object context) {
        this.context = context;
    }
}
