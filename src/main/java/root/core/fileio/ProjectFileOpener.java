package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassDecompiler;
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

    public ProjectFileOpener(ApplicationState applicationState, FileIO fileIO, ClassDecompiler classDecompiler) {
        this.applicationState = applicationState;
        this.fileIO = fileIO;
        this.classDecompiler = classDecompiler;
    }

    public FileReadResultDTO openNode (ProjectStructureTreeElementDTO[] nodesPath){
        List<String> nodeNames = Arrays.stream(nodesPath).map(ProjectStructureTreeElementDTO::getPath).collect(Collectors.toList());
        nodeNames = removeRootNodeIfItsMavenOrJDKPath(nodeNames);
        String[] nodes = nodeNames.toArray(new String[]{});


        Path path = Path.of("", nodes);
        FileReadResultDTO fileReadResultDTO;
        if (!nodeNames.isEmpty() && nodes[nodes.length-1].endsWith(".class")) {
            fileReadResultDTO = classDecompiler.decompile(nodes);
        }
        else if (path.toFile().isFile() && !path.toString().endsWith(".jar")) {
            fileReadResultDTO = fileIO.readFile(path);
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
