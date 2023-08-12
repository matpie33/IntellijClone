package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicatonState;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.TreeNodeFileDTO;
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

    private ApplicatonState applicatonState;

    public FileRenameListener(RenameFileDialogBuilder renameFileDialogBuilder, ApplicatonState applicatonState) {
        this.renameFileDialogBuilder = renameFileDialogBuilder;
        this.applicatonState = applicatonState;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<TreeNodeFileDTO[]> nodesSelections = context.getNodesPaths();
        TreeNodeFileDTO[] firstSelection = nodesSelections.get(0);
        TreePath[] selectedPaths = context.getSelectedPaths();
        if (selectedPaths.length==0){
            return;
        }
        TreePath selectedPath = selectedPaths[0];
        String projectPath = applicatonState.getProjectPath().getParent();
        String[] paths = Arrays.stream(firstSelection).map(TreeNodeFileDTO::getDisplayName).toArray(String [] :: new);
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
