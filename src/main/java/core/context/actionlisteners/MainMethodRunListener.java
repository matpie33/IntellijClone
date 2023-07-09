package core.context.actionlisteners;

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
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("run main method");
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
