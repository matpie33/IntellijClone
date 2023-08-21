package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.constants.DialogText;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.fileio.FileIO;
import root.core.ui.tree.ProjectStructureNode;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
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
        List<ProjectStructureNode[]> nodesPaths = context.getNodesPaths();
        TreePath[] selectedPaths = context.getSelectedPaths();
        if (nodesPaths.isEmpty()){
            return;
        }
        String objectToDelete = getObjectToDeleteName(nodesPaths);
        int result = JOptionPane.showConfirmDialog(Main.FRAME.getContentPane(),
                String.format(DialogText.CONFIRM_FILE_DELETE, objectToDelete));
        if (result == JOptionPane.YES_OPTION) {
            List<TreePath> deletedNodes = new ArrayList<>();
            for (TreePath path : selectedPaths) {
                boolean isDeleted = tryDeleteFile(path);
                if (isDeleted){
                    deletedNodes.add(path);
                }
            }
            context.setSelectionPaths(deletedNodes.toArray(new TreePath[] {}));
            uiEventsQueue.dispatchEvent(UIEventType.FILE_REMOVED_FROM_PROJECT, context);
        }
    }

    private boolean tryDeleteFile(TreePath path) {
        try {
            return fileIO.removeFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getObjectToDeleteName(List<ProjectStructureNode[]> nodesPaths) {
        String objectToDelete;
        if (nodesPaths.size()>1){
            objectToDelete = String.format("%d files", nodesPaths.size());
        }
        else{
            ProjectStructureNode[] paths = nodesPaths.iterator().next();
            objectToDelete = paths[paths.length-1].getDisplayName();

        }
        return objectToDelete;
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
