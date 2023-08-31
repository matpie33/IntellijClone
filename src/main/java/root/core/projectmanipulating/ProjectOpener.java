package root.core.projectmanipulating;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.classmanipulating.ClassStructureParser;
import root.core.directory.changesdetecting.DirectoriesWatcher;
import root.core.dto.ApplicationState;
import root.core.jdk.manipulating.JavaSourcesExtractor;
import root.core.mavencommands.MavenCommandsController;
import root.core.nodehandling.ProjectStructureNodesHandler;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.core.utility.ThreadExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectOpener {

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private UIEventsQueue uiEventsQueue;

    private ApplicationState applicationState;

    private DirectoriesWatcher directoriesWatcher;

    private ClassStructureParser classStructureParser;

    private ThreadExecutor threadExecutor;

    private MavenCommandsController mavenCommandsController;

    private JavaSourcesExtractor javaSourcesExtractor;

    public ProjectOpener(ProjectStructureNodesHandler projectStructureNodesHandler, UIEventsQueue uiEventsQueue, ApplicationState applicationState, DirectoriesWatcher directoriesWatcher, ClassStructureParser classStructureParser, ThreadExecutor threadExecutor, MavenCommandsController mavenCommandsController, JavaSourcesExtractor javaSourcesExtractor) {
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.uiEventsQueue = uiEventsQueue;
        this.applicationState = applicationState;
        this.directoriesWatcher = directoriesWatcher;
        this.classStructureParser = classStructureParser;
        this.threadExecutor = threadExecutor;
        this.mavenCommandsController = mavenCommandsController;
        this.javaSourcesExtractor = javaSourcesExtractor;
    }

    public void openProject (File rootDirectory){
        applicationState.setProjectPath(rootDirectory);
        List<File> files = getClasses(rootDirectory);
        classStructureParser.parseClassStructure(files, ClassOrigin.SOURCES);
        directoriesWatcher.watchProjectDirectoryForChanges();
        mavenCommandsController.init();
        threadExecutor.runTasksInMainPool(mavenCommandsController.getMavenTasks());
        threadExecutor.runTaskInMainPoolAfterMavenTaskDone(()->uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Maven tasks finished"));
        uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootDirectory);
    }

    private List<File> getClasses(File rootDirectory) {
        List<File> classes = new ArrayList<>();
        extractFiles(rootDirectory, classes);
        return classes;
    }

    private void extractFiles(File rootDirectory, List<File> classes) {
        for (File file : rootDirectory.listFiles()) {
            if (file.isDirectory()){
                extractFiles(file, classes);
            }
            else{
                if (file.getName().endsWith(".java")){
                    classes.add(file);
                }
            }
        }
    }


}
