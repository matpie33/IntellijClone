package core.backend;

import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JavaRunCommandBuilder {

    private ApplicatonState applicatonState;

    public JavaRunCommandBuilder(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public String[] build (String className){
        String classPath = applicatonState.getClassPath();
        List<String> commands = new ArrayList<>();
        commands.add("java");
        commands.add("-classpath");
        commands.add(classPath);
        commands.add( className.substring(0, className.indexOf(".java")));
        return commands.toArray(new String[]{});
    }

}
