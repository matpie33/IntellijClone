package core.context.actionlisteners;

import core.backend.RunCommandBuilder;
import core.dialogbuilders.RenameFileDialogBuilder;
import core.dto.ApplicatonState;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    private RunCommandBuilder runCommandBuilder;

    public MainMethodRunListener(RunCommandBuilder runCommandBuilder) {
        this.runCommandBuilder = runCommandBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            File selectedFile = context.getSelectedFile();
            String[] commands = runCommandBuilder.build(selectedFile.getName());

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
