package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassDecompiler;
import root.core.dto.ApplicatonState;
import root.core.dto.FileReadResultDTO;
import root.core.dto.TreeNodeFileDTO;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectFileOpener {

    private ApplicatonState applicatonState;

    private FileIO fileIO;

    private ClassDecompiler classDecompiler;

    public ProjectFileOpener(ApplicatonState applicatonState, FileIO fileIO, ClassDecompiler classDecompiler) {
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
        this.classDecompiler = classDecompiler;
    }

    public FileReadResultDTO openNode (TreeNodeFileDTO[] nodesPath){
        List<String> nodeNames = Arrays.stream(nodesPath).map(TreeNodeFileDTO::getPath).collect(Collectors.toList());
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
