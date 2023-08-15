package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassDecompiler;
import root.core.classmanipulating.ClassOrigin;
import root.core.classmanipulating.ClassStructureParser;
import root.core.dto.ApplicationState;
import root.core.dto.FileReadResultDTO;
import root.core.dto.ProjectStructureTreeElementDTO;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectFileOpener {

    private ApplicationState applicationState;

    private FileIO fileIO;

    private ClassDecompiler classDecompiler;

    private ClassStructureParser classStructureParser;

    public ProjectFileOpener(ApplicationState applicationState, FileIO fileIO, ClassDecompiler classDecompiler, ClassStructureParser classStructureParser) {
        this.applicationState = applicationState;
        this.fileIO = fileIO;
        this.classDecompiler = classDecompiler;
        this.classStructureParser = classStructureParser;
    }

    public FileReadResultDTO openNode (ProjectStructureTreeElementDTO[] nodesPath){
        List<String> nodeNames = Arrays.stream(nodesPath).map(ProjectStructureTreeElementDTO::getPath).collect(Collectors.toList());
        ClassOrigin classOrigin = nodeNames.contains("JDK")? ClassOrigin.JDK: nodeNames.contains("maven")? ClassOrigin.MAVEN: ClassOrigin.SOURCES;
        nodeNames = removeRootNodeIfItsMavenOrJDKPath(nodeNames);
        String[] nodes = nodeNames.toArray(new String[]{});


        Path path = Path.of("", nodes);
        return readFile(classOrigin, path);
    }

    public FileReadResultDTO readFile(ClassOrigin classOrigin, Path path) {
        FileReadResultDTO fileReadResultDTO;
        if (!path.toString().isEmpty() && path.toString().endsWith(".class")) {
            fileReadResultDTO = classDecompiler.decompile(path);
            String content = String.join("\n", fileReadResultDTO.getContentLines());
            classStructureParser.parseClassContent(content, fileReadResultDTO.getFile(), ClassOrigin.MAVEN);
        }
        else if (path.toFile().isFile() && !path.toString().endsWith(".jar")) {
            fileReadResultDTO = fileIO.readFile(path, classOrigin);
        }
        else{
            fileReadResultDTO = new FileReadResultDTO();
        }
        return fileReadResultDTO;
    }

    private List<String> removeRootNodeIfItsMavenOrJDKPath(List<String> nodeNames) {
        int jdkIndex = nodeNames.indexOf("JDK");
        int mavenIndex = nodeNames.indexOf("maven");
        if (jdkIndex != -1){
            nodeNames = nodeNames.subList(jdkIndex+1, nodeNames.size());
        }
        else if (mavenIndex != -1){
            nodeNames = nodeNames.subList(mavenIndex+1, nodeNames.size());
        }
        return nodeNames;
    }

}
