package root.core.menuitemlisteners;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.classmanipulating.ClassStructureParser;
import root.core.directory.changesdetecting.DirectoriesWatcher;
import root.core.dto.ApplicatonState;
import root.core.jdk.manipulating.JavaSourcesExtractor;
import root.core.mavencommands.MavenCommandsController;
import root.core.nodehandling.ProjectStructureNodesHandler;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.core.utility.ThreadExecutor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenProjectActionListener implements MenuItemListener {

    public static final int CLASSES_TO_PARSE_PER_THREAD = 20;
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
            File jdkSourcesRoot = javaSourcesExtractor.getJavaSourcesDirectory();
            List<File> classesGroup = new ArrayList<>();
            groupClassesToParseByThread(jdkSourcesRoot, CLASSES_TO_PARSE_PER_THREAD, classesGroup);
            if (!classesGroup.isEmpty()){
                final ArrayList<File> files = new ArrayList<>(classesGroup);
                threadExecutor.scheduleTask(()-> parseClasses(files));
            }


            projectStructureNodesHandler.addNodesForJDKSources(rootNode, jdkSourcesRoot);

            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootNode);
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
                    threadExecutor.scheduleTask(()-> parseClasses(finalList));
                    fileGroup.clear();
                    fileGroup.add(file);
                }
            }
            else{
                groupClassesToParseByThread(file, groupSize, fileGroup);
            }

        }
    }

    private void parseClasses(List<File> classesGroup) {
        for (File classFile : classesGroup) {
            classStructureParser.parseClassStructure(classFile);
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
