package root.core.mavencommands;

import org.springframework.stereotype.Component;
import root.core.directory.changesdetecting.DirectoryChangesDetector;
import root.core.dto.ApplicationState;
import root.core.dto.ErrorDTO;
import root.core.dto.MavenCommandResultDTO;
import root.core.jdk.manipulating.ClassesFromJarsExtractor;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class MavenCommandsController {

    private MavenCommandExecutor mavenCommandExecutor;

    private ApplicationState applicationState;

    private UIEventsQueue uiEventsQueue;

    private ClassesFromJarsExtractor classesFromJarsExtractor;

    private DirectoryChangesDetector directoryChangesDetector;

    public MavenCommandsController(MavenCommandExecutor mavenCommandExecutor, ApplicationState applicationState, UIEventsQueue uiEventsQueue, ClassesFromJarsExtractor classesFromJarsExtractor, DirectoryChangesDetector directoryChangesDetector) {
        this.mavenCommandExecutor = mavenCommandExecutor;
        this.applicationState = applicationState;
        this.uiEventsQueue = uiEventsQueue;
        this.classesFromJarsExtractor = classesFromJarsExtractor;
        this.directoryChangesDetector = directoryChangesDetector;
    }

    public void init (){
        mavenCommandExecutor.initialize();
    }

    public Runnable[] getMavenTasks() {
        String dialogErrorMessage = "Failed to run maven command. Check console";
        return new Runnable[] {
                ()-> readClassPathAndLocalRepositoryPath(dialogErrorMessage),
                ()-> cleanInstallTargetDirectory(dialogErrorMessage)
        };

    }


    private void cleanInstallTargetDirectory(String dialogErrorMessage) {
        runMvnCommand(dialogErrorMessage, new String[]{"clean"}, new String[]{"-Dmaven.test.skip"});
        directoryChangesDetector.checkForChangesInWatchedDirectories();
        runMvnCommand(dialogErrorMessage, new String[]{"install"}, new String[]{"-Dmaven.test.skip"});
        directoryChangesDetector.checkForChangesInWatchedDirectories();

    }

    private void readLocalRepositoryPath(String dialogErrorMessage) {
        MavenCommandResultDTO localRepositoryResult = runMvnCommand(dialogErrorMessage, new String[]{"help:evaluate"}, new String[]{"-Dexpression=settings.localRepository", "-q", "-DforceStdout"});
        applicationState.setLocalRepositoryPath(localRepositoryResult.getOutput().trim());
    }

    private void readClassPathAndLocalRepositoryPath(String dialogErrorMessage) {
        readLocalRepositoryPath(dialogErrorMessage);
        readClassPath(dialogErrorMessage);
    }

    private void readClassPath(String dialogErrorMessage) {
        MavenCommandResultDTO readClasspathResult = runMvnCommand(dialogErrorMessage, new String[]{"exec:exec"}, new String[]{"-Dexec.executable=cmd","-q", "-Dexec.args='/c echo %classpath'"});

        String classPath = readClasspathResult.getOutput().replace("\"", ";");
        int outputDirectoryIndex = classPath.indexOf(applicationState.getProjectPath().toString());
        String outputDirectory = classPath.substring(outputDirectoryIndex, classPath.indexOf(";", outputDirectoryIndex));
        applicationState.setBuildOutputDirectory(outputDirectory);
        applicationState.setClassPath(classPath);
        Map<String, List<File>> jarToClassesMap = classesFromJarsExtractor.extractClassesFromJars(classPath);
        uiEventsQueue.dispatchEvent(UIEventType.MAVEN_CLASSPATH_READED, jarToClassesMap);
    }

    private MavenCommandResultDTO runMvnCommand(String dialogMessage, String[] goal, String[] args) {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, String.format("Executing maven command: %s with args %s", Arrays.toString(goal), Arrays.toString(args)));
        MavenCommandResultDTO evaluateBuildDirectoryResult = mavenCommandExecutor.runCommandInConsole(goal, args);
        if (!evaluateBuildDirectoryResult.isSuccess()){
            String errorMessage = String.format("Failed to run %s, %s: ", Arrays.toString(goal), evaluateBuildDirectoryResult.getOutput());
            RuntimeException exception = new RuntimeException(errorMessage);
            uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO(dialogMessage, exception));
            throw exception;
        }
        return evaluateBuildDirectoryResult;
    }

    public void interrupt() {
        mavenCommandExecutor.interrupt();
    }
}
