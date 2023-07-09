package core.menuitemlisteners;

import core.*;
import core.backend.DirectoriesWatcher;
import core.backend.MainMethodSeeker;
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

    private DirectoriesWatcher directoriesWatcher;

    private MainMethodSeeker mainMethodSeeker;

    public OpenProjectActionListener(ProjectStructureReader projectStructureReader, ProjectStructureBuilderUI projectStructureBuilderUI, UIEventsQueue uiEventsQueue, ApplicatonState applicatonState, DirectoriesWatcher directoriesWatcher, MainMethodSeeker mainMethodSeeker) {
        this.projectStructureReader = projectStructureReader;
        this.projectStructureBuilderUI = projectStructureBuilderUI;
        this.uiEventsQueue = uiEventsQueue;
        this.applicatonState = applicatonState;
        this.directoriesWatcher = directoriesWatcher;
        this.mainMethodSeeker = mainMethodSeeker;
        jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int action = jFileChooser.showOpenDialog(Main.FRAME);
        if (action == JFileChooser.APPROVE_OPTION){
            File rootDirectory = jFileChooser.getSelectedFile();
            cacheClassesWithMainMethods(rootDirectory);
            applicatonState.setProjectRootDirectoryName(rootDirectory.getName());
            applicatonState.setProjectPath(rootDirectory.getParent());
            directoriesWatcher.watchProjectDirectory();
            List<FileDTO> files = projectStructureReader.readProjectDirectory(rootDirectory);
            DefaultMutableTreeNode rootNode = projectStructureBuilderUI.build(rootDirectory, files);
            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootNode);
        }
    }

    private void cacheClassesWithMainMethods(File rootDirectory) {
        for (File file : rootDirectory.listFiles()) {
            if (file.isDirectory()){
                cacheClassesWithMainMethods(file);
            }
            else{
                if (file.getName().endsWith(".java")){
                    boolean isMain = mainMethodSeeker.findMainMethod(file);
                    if (isMain){
                        applicatonState.addClassWithMainMethod(file);
                    }
                }
            }
        }
    }


    @Override
    public String getName() {
        return "Open";
    }
}
