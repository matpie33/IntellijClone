package core.menuitemlisteners;

import core.*;
import core.backend.MavenCommandExecutor;
import core.backend.DirectoriesWatcher;
import core.backend.MainMethodSeeker;
import core.backend.ThreadExecutor;
import core.dto.ApplicatonState;
import core.dto.FileDTO;
import core.dto.MavenCommandResultDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;
import core.uibuilders.ProjectStructureBuilderUI;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private MavenCommandExecutor mavenCommandExecutor;

    private ThreadExecutor threadExecutor;

    public OpenProjectActionListener(ProjectStructureReader projectStructureReader, ProjectStructureBuilderUI projectStructureBuilderUI, UIEventsQueue uiEventsQueue, ApplicatonState applicatonState, DirectoriesWatcher directoriesWatcher, MainMethodSeeker mainMethodSeeker, MavenCommandExecutor mavenCommandExecutor, ThreadExecutor threadExecutor) {
        this.projectStructureReader = projectStructureReader;
        this.projectStructureBuilderUI = projectStructureBuilderUI;
        this.uiEventsQueue = uiEventsQueue;
        this.applicatonState = applicatonState;
        this.directoriesWatcher = directoriesWatcher;
        this.mainMethodSeeker = mainMethodSeeker;
        this.mavenCommandExecutor = mavenCommandExecutor;
        this.threadExecutor = threadExecutor;
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
            directoriesWatcher.watchProjectDirectory();
            List<FileDTO> files = projectStructureReader.readProjectDirectory(rootDirectory);
            DefaultMutableTreeNode rootNode = projectStructureBuilderUI.build(rootDirectory, files);
            threadExecutor.addReadClassPathMavenTask(this::readClassPath);
            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_OPENED, rootNode);
        }
    }

    private void readClassPath() {
        mavenCommandExecutor.initialize();
        MavenCommandResultDTO firstResult = mavenCommandExecutor.runCommandWithFileOutput("dependency:build-classpath", String.format("-Dmdep.outputFile=%s", "cp.txt"));
        if (!firstResult.isSuccess()){
            JOptionPane.showMessageDialog(Main.FRAME, "Failed to run mvn command. Check console");
            uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, firstResult.getOutput());
            throw new RuntimeException("Failed to run maven read class path command");
        }
        MavenCommandResultDTO commandResult = mavenCommandExecutor.runCommandInConsole("help:evaluate", "-Dexpression=project.build.outputDirectory", "-q", "-DforceStdout");
        boolean fileExists = Path.of(commandResult.getOutput().replace("\n", "")).toFile().exists();
        if (!fileExists || !commandResult.isSuccess()){
            JOptionPane.showMessageDialog(Main.FRAME, "Failed to run mvn command. Check console");
            String message = "Failed to run help:evaluate, message: "+commandResult.getOutput();
            uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, message);
        }
        try {
            List<String> classPathValues = Files.readAllLines(firstResult.getOutputFile().toPath());
            String outputDirectory = commandResult.getOutput().trim();
            applicatonState.setOutputDirectory(outputDirectory);
            classPathValues.add(";"+ outputDirectory);
            String fullClasspath = String.join("", classPathValues);
            applicatonState.setClassPath(fullClasspath);
            boolean isDeleted = firstResult.getOutputFile().delete();
            if (!isDeleted){
                System.err.println("file is not deleted");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
