package core.menuitemlisteners;

import core.*;
import core.dto.ApplicatonState;
import core.dto.FileDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;
import core.uibuilders.ProjectStructureBuilderUI;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

@Component
public class OpenProjectActionListener implements MenuItemListener {

    private JFileChooser jFileChooser;

    private ProjectStructureReader projectStructureReader;

    private ProjectStructureBuilderUI projectStructureBuilderUI;

    private UIEventsQueue uiEventsQueue;

    private ApplicatonState applicatonState;


    public OpenProjectActionListener(ProjectStructureReader projectStructureReader, ProjectStructureBuilderUI projectStructureBuilderUI, UIEventsQueue uiEventsQueue, ApplicatonState applicatonState) {
        this.projectStructureReader = projectStructureReader;
        this.projectStructureBuilderUI = projectStructureBuilderUI;
        this.uiEventsQueue = uiEventsQueue;
        this.applicatonState = applicatonState;
        jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int action = jFileChooser.showOpenDialog(Main.FRAME);
        if (action == JFileChooser.APPROVE_OPTION){
            File selectedFile = jFileChooser.getSelectedFile();
            applicatonState.setProjectPath(selectedFile.getParent());
            List<FileDTO> files = projectStructureReader.readProjectDirectory(selectedFile);
            DefaultMutableTreeNode rootNode = projectStructureBuilderUI.build(selectedFile, files);
            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootNode);
        }
    }

    @Override
    public String getName() {
        return "Open";
    }
}
