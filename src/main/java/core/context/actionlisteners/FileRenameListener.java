package core.context.actionlisteners;

import core.context.providers.NodePathManipulation;
import core.dialogbuilders.RenameFileDialogBuilder;
import core.dto.ApplicatonState;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Component
public class FileRenameListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    private RenameFileDialogBuilder renameFileDialogBuilder;

    private ApplicatonState applicatonState;

    public FileRenameListener(RenameFileDialogBuilder renameFileDialogBuilder, ApplicatonState applicatonState) {
        this.renameFileDialogBuilder = renameFileDialogBuilder;
        this.applicatonState = applicatonState;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<String[]> nodesPaths = context.getNodesPaths();
        String[] nodes = nodesPaths.iterator().next();
        TreePath[] selectedPaths = context.getSelectedPaths();
        if (selectedPaths.length==0){
            return;
        }
        TreePath selectedPath = selectedPaths[0];
        String projectPath = applicatonState.getProjectPath().getParent();
        Path path = Path.of(projectPath, nodes);
        File file = path.toFile();
        Point position = context.getPosition();
        renameFileDialogBuilder.showDialog(position, file, (DefaultMutableTreeNode)selectedPath.getLastPathComponent());
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
