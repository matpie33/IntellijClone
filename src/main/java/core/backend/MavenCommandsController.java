package core.backend;

import core.dto.ApplicatonState;
import core.dto.ErrorDTO;
import core.dto.MavenCommandResultDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class MavenCommandsController {

    private MavenCommandExecutor mavenCommandExecutor;

    private ApplicatonState applicatonState;

    private UIEventsQueue uiEventsQueue;

    private ClassesFromJarsExtractor classesFromJarsExtractor;

    public MavenCommandsController(MavenCommandExecutor mavenCommandExecutor, ApplicatonState applicatonState, UIEventsQueue uiEventsQueue, ClassesFromJarsExtractor classesFromJarsExtractor) {
        this.mavenCommandExecutor = mavenCommandExecutor;
        this.applicatonState = applicatonState;
        this.uiEventsQueue = uiEventsQueue;
        this.classesFromJarsExtractor = classesFromJarsExtractor;
    }

    public void executeMavenCommands() {
        mavenCommandExecutor.initialize();
        String dialogErrorMessage = "Failed to run maven command. Check console";
        MavenCommandResultDTO readClasspathResult = runMvnCommand(dialogErrorMessage, new String[]{"exec:exec"}, new String[]{"-Dexec.executable=cmd","-q", "-Dexec.args='/c echo %classpath'"});

        String classPath = readClasspathResult.getOutput().replace("\"", ";");
        int outputDirectoryIndex = classPath.indexOf(applicatonState.getProjectPath().toString());
        String outputDirectory = classPath.substring(outputDirectoryIndex, classPath.indexOf(";", outputDirectoryIndex));
        applicatonState.setOutputDirectory(outputDirectory);
        applicatonState.setClassPath(classPath);

        Map<String, List<File>> jarToClassesMap = classesFromJarsExtractor.extractClassesFromJars(classPath);

        MavenCommandResultDTO localRepositoryResult = runMvnCommand(dialogErrorMessage, new String[]{"help:evaluate"}, new String[]{"-Dexpression=settings.localRepository", "-q", "-DforceStdout"});
        applicatonState.setLocalRepositoryPath(localRepositoryResult.getOutput().trim());
        uiEventsQueue.dispatchEvent(UIEventType.MAVEN_CLASSPATH_READED, jarToClassesMap);

        runMvnCommand(dialogErrorMessage, new String[]{"clean","install"}, new String[]{"-Dmaven.test.skip"});
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

    private MavenCommandResultDTO runMvnCommandGetClasspath(String dialogMessage) {
        String command = "dependency:build-classpath";
        String args = String.format("-Dmdep.outputFile=%s", "cp.txt");
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, String.format("Executing maven command: %s with args %s", command, args));
        MavenCommandResultDTO buildClassPathResult = mavenCommandExecutor.runCommandWithFileOutput(command, args);
        if (!buildClassPathResult.isSuccess()){
            RuntimeException exception = new RuntimeException("Failed to run maven read class path command\n" + buildClassPathResult.getOutput());
            uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO(dialogMessage, exception));
            throw exception;
        }
        return buildClassPathResult;
    }

    public void interrupt() {
        mavenCommandExecutor.interrupt();
    }
}
