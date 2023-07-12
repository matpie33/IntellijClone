package core.backend;

import core.dto.ApplicatonState;
import core.dto.ErrorDTO;
import core.dto.MavenCommandResultDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Component
public class MavenCommandsController {

    private MavenCommandExecutor mavenCommandExecutor;

    private ApplicatonState applicatonState;

    private UIEventsQueue uiEventsQueue;

    public MavenCommandsController(MavenCommandExecutor mavenCommandExecutor, ApplicatonState applicatonState, UIEventsQueue uiEventsQueue) {
        this.mavenCommandExecutor = mavenCommandExecutor;
        this.applicatonState = applicatonState;
        this.uiEventsQueue = uiEventsQueue;
    }

    public void executeMavenCommands() {
        mavenCommandExecutor.initialize();
        String dialogErrorMessage = "Failed to run maven command. Check console";
        MavenCommandResultDTO buildClassPathResult = runMvnCommandGetClasspath(dialogErrorMessage);
        MavenCommandResultDTO evaluateBuildDirectoryResult = runMvnCommand(dialogErrorMessage, new String[]{"help:evaluate"}, new String[]{"-Dexpression=project.build.outputDirectory","-q", "-DforceStdout"},
                getEvaluateParameterResultValidation());
        runMvnCommand(dialogErrorMessage, new String[]{"clean","install"}, new String[]{"-Dmaven.test.skip"}, null);
        try {
            List<String> classPathValues = Files.readAllLines(buildClassPathResult.getOutputFile().toPath());
            String outputDirectory = evaluateBuildDirectoryResult.getOutput().trim();
            applicatonState.setOutputDirectory(outputDirectory);
            classPathValues.add(";"+ outputDirectory);
            String fullClasspath = String.join("", classPathValues);
            applicatonState.setClassPath(fullClasspath);
            boolean isDeleted = buildClassPathResult.getOutputFile().delete();
            if (!isDeleted){
                System.err.println("file is not deleted");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Function<MavenCommandResultDTO, Boolean> getEvaluateParameterResultValidation() {
        return result -> Path.of(result.getOutput().replace("\n", "")).toFile().exists();
    }

    private MavenCommandResultDTO runMvnCommand(String dialogMessage, String[] goal, String[] args, Function<MavenCommandResultDTO, Boolean> customValidation) {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, String.format("Executing maven command: %s with args %s", Arrays.toString(goal), Arrays.toString(args)));
        MavenCommandResultDTO evaluateBuildDirectoryResult = mavenCommandExecutor.runCommandInConsole(goal, args);
        boolean isSuccess;
        if (customValidation != null){
            isSuccess = customValidation.apply(evaluateBuildDirectoryResult);
        }
        else{
            isSuccess = evaluateBuildDirectoryResult.isSuccess();
        }
        if (!isSuccess){
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
}
