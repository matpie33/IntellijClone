package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassDecompiler;
import root.core.classmanipulating.ClassOrigin;
import root.core.classmanipulating.ClassStructureParser;
import root.core.dto.ApplicationState;
import root.core.dto.FileReadResultDTO;
import root.core.jdk.manipulating.JavaSourcesExtractor;
import root.core.ui.tree.ProjectStructureNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectFileOpener {

    private ApplicationState applicationState;

    private FileIO fileIO;

    private ClassDecompiler classDecompiler;

    private ClassStructureParser classStructureParser;

    private JavaSourcesExtractor javaSourcesExtractor;

    public ProjectFileOpener(ApplicationState applicationState, FileIO fileIO, ClassDecompiler classDecompiler, ClassStructureParser classStructureParser, JavaSourcesExtractor javaSourcesExtractor) {
        this.applicationState = applicationState;
        this.fileIO = fileIO;
        this.classDecompiler = classDecompiler;
        this.classStructureParser = classStructureParser;
        this.javaSourcesExtractor = javaSourcesExtractor;
    }

    public FileReadResultDTO openNode (ProjectStructureNode[] nodesPath, ClassOrigin classOrigin){
        String rootDirectory;
        List<String> paths = new ArrayList<>();
        for (ProjectStructureNode node : nodesPath) {
            paths.add(node.getFilePath());
        }
        String[] nodes = paths.toArray(new String[]{});

        switch (classOrigin){
            case SOURCES:
                rootDirectory = applicationState.getProjectPath().toString();
                break;
            case JDK:
            case MAVEN:
                rootDirectory = "";
                break;
            default: throw new RuntimeException();

        }

        Path path = Path.of(rootDirectory, nodes);
        return readFile(classOrigin, path);
    }

    public FileReadResultDTO readFile(ClassOrigin classOrigin, Path path) {
        FileReadResultDTO fileReadResultDTO;
        if (!path.toString().isEmpty() && path.toString().endsWith(".class")) {
            fileReadResultDTO = classDecompiler.decompile(path);
            String content = String.join("\n", fileReadResultDTO.getContentLines());
            classStructureParser.parseClassContent(fileReadResultDTO.getFile(), ClassOrigin.MAVEN);
        }
        else if (path.toFile().isFile() && !path.toString().endsWith(".jar")) {
            fileReadResultDTO = fileIO.readFile(path, classOrigin);
        }
        else{
            fileReadResultDTO = new FileReadResultDTO();
        }
        return fileReadResultDTO;
    }


}
