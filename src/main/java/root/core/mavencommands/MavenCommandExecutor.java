package root.core.mavencommands;

import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Component;
import root.core.dto.ApplicatonState;
import root.core.dto.MavenCommandResultDTO;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Component
public class MavenCommandExecutor {

    private ApplicatonState applicatonState;

    private String createdFileName = "cp.txt";
    private File pomFile;

    private boolean interrupted;

    public MavenCommandExecutor(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public void initialize (){
        System.setProperty("maven.home", System.getenv("maven.home"));
        String projectPath = applicatonState.getProjectPath().toString();
        pomFile = new File(projectPath + "/pom.xml");
        interrupted = false;
    }

    public MavenCommandResultDTO runCommandWithFileOutput(String command, String... args) {

        InvocationRequest request = createInvocationRequest(new String[]{command}, args);
        request.setQuiet(true);

        DefaultInvoker defaultInvoker = new DefaultInvoker();
        StringBuilder builder = addOutputHandler(defaultInvoker);

        try {
            InvocationResult result = defaultInvoker.execute(request);
            if (interrupted){
                interrupted= false;
                throw new RuntimeException("Interrupted");
            }
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

    private InvocationRequest createInvocationRequest(String[] goals, String[] arguments) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setInputStream(InputStream.nullInputStream());
        request.setGoals(List.of(goals));
        request.setPomFile(pomFile);
        for (String arg : arguments) {
            request.addArg(arg);
        }
        return request;
    }

    public MavenCommandResultDTO runCommandInConsole (String[] goals, String... arguments){
        InvocationRequest invocationRequest = createInvocationRequest(goals, arguments);
        DefaultInvoker defaultInvoker = new DefaultInvoker();
        StringBuilder output = addOutputHandler(defaultInvoker);
        try {
            InvocationResult result = defaultInvoker.execute(invocationRequest);
            if (interrupted){
                interrupted= false;
                throw new RuntimeException("Interrupted");
            }
            return new MavenCommandResultDTO(result.getExitCode()==0, output.toString());
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }

    }

    public void interrupt (){
        interrupted = true;
    }



}
