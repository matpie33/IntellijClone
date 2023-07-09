package core.backend;

import core.dto.ApplicatonState;
import core.dto.MavenCommandResultDTO;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Component
public class MavenCommandExecutor {

    private ApplicatonState applicatonState;

    private String createdFileName = "cp.txt";
    private File pomFile;

    public MavenCommandExecutor(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public void initialize (){
        System.setProperty("maven.home", System.getenv("maven.home"));
        String projectPath = applicatonState.getProjectPath();
        Path projectPathObject = Path.of(projectPath, applicatonState.getProjectRootDirectoryName());
        pomFile = new File(projectPathObject + "/pom.xml");
    }

    public MavenCommandResultDTO runCommandWithFileOutput(String command, String... args) {

        InvocationRequest request = createInvocationRequest(command, args);
        request.setQuiet(true);

        DefaultInvoker defaultInvoker = new DefaultInvoker();
        StringBuilder builder = addOutputHandler(defaultInvoker);

        try {
            InvocationResult result = defaultInvoker.execute(request);
            Path createdFile = pomFile.getParentFile().toPath().resolve(createdFileName);
            MavenCommandResultDTO mavenCommandResultDTO = new MavenCommandResultDTO(result.getExitCode() == 0, builder.toString());
            mavenCommandResultDTO.setOutputFile(createdFile.toFile());
            return mavenCommandResultDTO;

        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }


    }

    private StringBuilder addOutputHandler(DefaultInvoker defaultInvoker) {
        StringBuilder builder = new StringBuilder();

        defaultInvoker.setOutputHandler(line -> {
            builder.append(line);
            builder.append("\n");
        });
        return builder;
    }

    private InvocationRequest createInvocationRequest(String command, String[] arguments) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setInputStream(InputStream.nullInputStream());
        request.setGoals(List.of(command));
        request.setPomFile(pomFile);
        for (String arg : arguments) {
            request.addArg(arg);
        }
        return request;
    }

    public MavenCommandResultDTO runCommandInConsole (String goal, String... arguments){
        InvocationRequest invocationRequest = createInvocationRequest(goal, arguments);
        DefaultInvoker defaultInvoker = new DefaultInvoker();
        StringBuilder output = addOutputHandler(defaultInvoker);
        try {
            InvocationResult result = defaultInvoker.execute(invocationRequest);
            return new MavenCommandResultDTO(result.getExitCode()==0, output.toString());
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }

    }


}
