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

import javax.swing.tree.DefaultMutableTreeNode;
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

    public static final int CLASSES_TO_PARSE_PER_THREAD = 50;

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
        parseClasses(rootDirectory, ClassOrigin.SOURCES);
        directoriesWatcher.watchProjectDirectoryForChanges();
        mavenCommandsController.init();
        threadExecutor.runTasksInMainPool(mavenCommandsController.getMavenTasks());
        threadExecutor.runTaskInMainPoolAfterMavenTaskDone(()->uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Maven tasks finished"));
        DefaultMutableTreeNode rootNode = projectStructureNodesHandler.addNodesForSources(rootDirectory, false);
        File jdkSourcesRoot = javaSourcesExtractor.getJavaSourcesDirectory().toFile();
        projectStructureNodesHandler.addNodesForJDKSources(rootNode, jdkSourcesRoot);
        parseJdkSources(jdkSourcesRoot);
        uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootNode);
    }

    private void parseJdkSources(File jdkSourcesRoot) {

        List<File> classesGroup = new ArrayList<>();
        groupClassesToParseByThread(jdkSourcesRoot, CLASSES_TO_PARSE_PER_THREAD, classesGroup);
        if (!classesGroup.isEmpty()){
            final ArrayList<File> files = new ArrayList<>(classesGroup);
            threadExecutor.runTaskInJdkPoolAfterMavenTaskDone(()-> parseClasses(files, ClassOrigin.JDK));
        }
    }

    private void groupClassesToParseByThread(File jdkSourcesRoot, int groupSize, List<File> fileGroup) {
        for (File file : jdkSourcesRoot.listFiles()) {
            if (file.isFile()){
                if (fileGroup.size()< groupSize){
                    fileGroup.add(file);
                }
                else{
                    ArrayList<File> finalList = new ArrayList<>(fileGroup);
                    threadExecutor.runTaskInMainPoolAfterMavenTaskDone(()-> parseClasses(finalList, ClassOrigin.JDK));
                    fileGroup.clear();
                    fileGroup.add(file);
                }
            }
            else{
                groupClassesToParseByThread(file, groupSize, fileGroup);
            }

        }
    }

    private void parseClasses(List<File> classesGroup, ClassOrigin origin) {
        for (File classFile : classesGroup) {
            classStructureParser.parseClassStructure(classFile, origin);
        }
    }


    private void parseClasses(File rootDirectory, ClassOrigin origin) {
        for (File file : rootDirectory.listFiles()) {
            if (file.isDirectory()){
                parseClasses(file, ClassOrigin.SOURCES);
            }
            else{
                if (file.getName().endsWith(".java")){
                    boolean isMain = classStructureParser.parseClassStructure(file, origin);
                    if (isMain){
                        applicationState.addClassWithMainMethod(file);
                    }
                }
            }
        }
    }


}
