package core.backend;

import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileIO {

    private ApplicatonState applicatonState;

    public FileIO(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public List<String> read(String[] directories){
        String projectPath = applicatonState.getProjectPath();
        try {
            Path path = Path.of(projectPath, directories);
            File file = path.toFile();
            if (file.isDirectory()){
                return new ArrayList<>();
            }
            applicatonState.setOpenedFile(file);
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(String text){
        File openedFile = applicatonState.getOpenedFile();
        try {
            Files.writeString(openedFile.toPath(), text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
