package root.core.process.commandline;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class JavaRunCommandBuilder {

    private ApplicationState applicationState;

    public JavaRunCommandBuilder(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public String[] createCommandForRunningMainClass(File mainClass){
        Path projectDirectory = applicationState.getProjectPath().toPath();
        Path srcRootRelative = Path.of("src", "main", "java");
        Path sourcesRootDirectory = projectDirectory.resolve(srcRootRelative);
        String pathToMainClass = sourcesRootDirectory.relativize(mainClass.toPath()).toString()
                .replace(".java", "");

        String classPath = applicationState.getClassPath();
        List<String> commands = new ArrayList<>();
        commands.add("java");
        commands.add("-classpath");
        commands.add(classPath);
        commands.add(pathToMainClass);
        return commands.toArray(new String[]{});

    }

    public String[] createCommandForCompilingClass(Set<File> classes){
        Path projectDirectory = applicationState.getProjectPath().toPath();
        String pathsToClasses = getPathsToFiles(classes, projectDirectory);
        String classPath = applicationState.getClassPath();
        List<String> commands = new ArrayList<>();
        String outputDirectory = getOutputDirectory(projectDirectory);
        commands.add("javac");
        commands.add("-classpath");
        commands.add(classPath);
        commands.add( "-d");
        commands.add( outputDirectory);

        String[] pathsToClassesByWords = pathsToClasses.split(" ");
        commands.addAll(Arrays.asList(pathsToClassesByWords));
        return commands.toArray(new String[]{});

    }

    private String getOutputDirectory(Path projectDirectory) {
        Path outputDirectory = Path.of(applicationState.getBuildOutputDirectory());
        Path outputDirectoryRelativeToProjectDirectory = projectDirectory.relativize(outputDirectory);
        return outputDirectoryRelativeToProjectDirectory.toString();
    }

    private String getPathsToFiles(Set<File> classes, Path projectDirectory) {
        StringBuilder paths = new StringBuilder();
        for (File aClass : classes) {
            String relativePathToFile = projectDirectory.relativize(aClass.toPath()).toString();
            paths.append(relativePathToFile);
            paths.append(" ");
        }
        return paths.toString().trim();
    }

}
