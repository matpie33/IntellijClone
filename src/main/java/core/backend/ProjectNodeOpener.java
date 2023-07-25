package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.dto.TreeNodeFileDTO;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;

@Component
public class ProjectNodeOpener {

    private ApplicatonState applicatonState;

    private FileIO fileIO;

    private ClassDecompiler classDecompiler;

    public ProjectNodeOpener(ApplicatonState applicatonState, FileIO fileIO, ClassDecompiler classDecompiler) {
        this.applicatonState = applicatonState;
        this.fileIO = fileIO;
        this.classDecompiler = classDecompiler;
    }

    public FileReadResultDTO openNode (TreeNodeFileDTO[] nodesPath){
        String projectPath = applicatonState.getProjectPath().getParent();
        String[] nodeNames = Arrays.stream(nodesPath).map(TreeNodeFileDTO::getDisplayName).toArray(String[]::new);
        Path path = Path.of(projectPath, nodeNames);
        FileReadResultDTO fileReadResultDTO;
        if (nodeNames[nodeNames.length-1].endsWith(".class")) {
            fileReadResultDTO = classDecompiler.decompile(nodesPath);
        }
        else if (path.toFile().exists()) {
            fileReadResultDTO = fileIO.readFile(path);
        }
        else{
            fileReadResultDTO = new FileReadResultDTO();
        }
        return fileReadResultDTO;
    }

}
