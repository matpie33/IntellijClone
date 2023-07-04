package core.backend;

import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;

@Component
public class CommandLineRunner {

    public void runCommand (String command, File projectDir) throws IOException {

        InvocationRequest request = new DefaultInvocationRequest();
        System.setProperty("maven.home", System.getenv("maven.home"));

        request.setGoals(Arrays.asList("compiler:compile"));
        request.setPomFile(new File(projectDir.getAbsolutePath()+"/pom.xml"));

        DefaultInvoker defaultInvoker = new DefaultInvoker();
        StringBuilder builder = new StringBuilder();
        builder.append("Running maven");

        defaultInvoker.setOutputHandler(line -> {
            builder.append(line);
            builder.append("\n");
        });
        try {
            InvocationResult execute = defaultInvoker.execute(request);
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }


    }


}
