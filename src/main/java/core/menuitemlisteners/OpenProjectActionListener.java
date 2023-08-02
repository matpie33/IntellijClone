package core.menuitemlisteners;

import core.Main;
import core.backend.*;
import core.dto.ApplicatonState;
import core.uibuilders.ProjectStructureNodesHandler;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.io.File;

@Component
public class OpenProjectActionListener implements MenuItemListener {

    private JFileChooser jFileChooser;


    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private UIEventsQueue uiEventsQueue;

    private ApplicatonState applicatonState;

    private DirectoriesWatcher directoriesWatcher;

    private ClassStructureParser classStructureParser;

    private ThreadExecutor threadExecutor;

    private MavenCommandsController mavenCommandsController;

    private JavaSourcesExtractor javaSourcesExtractor;

    public OpenProjectActionListener(ProjectStructureNodesHandler projectStructureNodesHandler, UIEventsQueue uiEventsQueue, ApplicatonState applicatonState, DirectoriesWatcher directoriesWatcher, ClassStructureParser classStructureParser, ThreadExecutor threadExecutor, MavenCommandsController mavenCommandsController, JavaSourcesExtractor javaSourcesExtractor) {
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.uiEventsQueue = uiEventsQueue;
        this.applicatonState = applicatonState;
        this.directoriesWatcher = directoriesWatcher;
        this.classStructureParser = classStructureParser;
        this.threadExecutor = threadExecutor;
        this.mavenCommandsController = mavenCommandsController;
        this.javaSourcesExtractor = javaSourcesExtractor;
        jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int action = jFileChooser.showOpenDialog(Main.FRAME);
        if (action == JFileChooser.APPROVE_OPTION){
            File rootDirectory = jFileChooser.getSelectedFile();
            cacheClassesWithMainMethods(rootDirectory);
            applicatonState.setProjectPath(rootDirectory);
            directoriesWatcher.watchProjectDirectoryForChanges();
            mavenCommandsController.interrupt();
            threadExecutor.addReadClassPathMavenTask(mavenCommandsController::executeMavenCommands);
            DefaultMutableTreeNode rootNode = projectStructureNodesHandler.addNodesForSources(rootDirectory, false);
            projectStructureNodesHandler.addNodesForJDKSources(rootNode, javaSourcesExtractor.getJavaSourcesDirectory());

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
                    boolean isMain = classStructureParser.parseClassStructure(file);
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
