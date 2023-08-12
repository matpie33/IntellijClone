package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.ProjectStructureTreeElementDTO;
import root.ui.dialogbuilders.RenameFileDialogBuilder;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
public class FileRenameListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    private RenameFileDialogBuilder renameFileDialogBuilder;

    private ApplicationState applicationState;

    public FileRenameListener(RenameFileDialogBuilder renameFileDialogBuilder, ApplicationState applicationState) {
        this.renameFileDialogBuilder = renameFileDialogBuilder;
        this.applicationState = applicationState;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<ProjectStructureTreeElementDTO[]> nodesSelections = context.getNodesPaths();
        ProjectStructureTreeElementDTO[] firstSelection = nodesSelections.get(0);
        TreePath[] selectedPaths = context.getSelectedPaths();
        if (selectedPaths.length==0){
            return;
        }
        TreePath selectedPath = selectedPaths[0];
        String projectPath = applicationState.getProjectPath().getParent();
        String[] paths = Arrays.stream(firstSelection).map(ProjectStructureTreeElementDTO::getDisplayName).toArray(String [] :: new);
        Path path = Path.of(projectPath, paths);
        File file = path.toFile();
        Point position = context.getPosition();
        renameFileDialogBuilder.showDialog(position, file, (DefaultMutableTreeNode)selectedPath.getLastPathComponent());
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
