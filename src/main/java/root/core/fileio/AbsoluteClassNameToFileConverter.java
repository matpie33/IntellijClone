package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.dto.ApplicationState;
import root.core.dto.ClassSuggestionDTO;
import root.core.jdk.manipulating.JavaSourcesExtractor;

import java.io.File;
import java.nio.file.Path;

@Component
public class AbsoluteClassNameToFileConverter {

    private ApplicationState applicationState;

    private JavaSourcesExtractor javaSourcesExtractor;

    public AbsoluteClassNameToFileConverter(ApplicationState applicationState, JavaSourcesExtractor jdkConfigurationHolder) {
        this.applicationState = applicationState;
        this.javaSourcesExtractor = jdkConfigurationHolder;
    }

    public File convertToFile (ClassSuggestionDTO classSuggestionDTO){
        String packageNameWithSlashes = classSuggestionDTO.getPackageName().replace('.', '/');

        ClassOrigin classOrigin = classSuggestionDTO.getClassOrigin();
        Path pathToFile;
        switch (classOrigin){
            case JDK:
                Path javaSourcesDirectory = javaSourcesExtractor.getJavaSourcesDirectory();
                pathToFile = javaSourcesDirectory
                        .resolve(classSuggestionDTO.getRootDirectory())
                        .resolve(packageNameWithSlashes)
                        .resolve(classSuggestionDTO.getClassName()+".java"); //TODO check other places where we append .java/class suffix
                break;
            case MAVEN:
                pathToFile = Path.of(classSuggestionDTO.getRootDirectory(), packageNameWithSlashes);
                pathToFile = pathToFile.resolve(classSuggestionDTO.getClassName()+".class");
                break;
            case SOURCES:
                Path projectPath = applicationState.getProjectPath().toPath();
                pathToFile = projectPath.resolve("src").resolve("main").resolve("java")
                    .resolve(packageNameWithSlashes)
                    .resolve(classSuggestionDTO.getClassName()+".java");
                break;
            default:
                throw new RuntimeException("Not supported enum value: "+classOrigin);
        }

        return pathToFile.toFile();
    }


}


