package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
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

    public FileReadResultDTO read(String[] directories){
        String projectPath = applicatonState.getProjectPath();
        try {
            Path path = Path.of(projectPath, directories);
            File file = path.toFile();
            if (file.isDirectory()){
                FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
                return fileReadResultDTO;
            }
            applicatonState.setOpenedFile(file);
            List<String> lines = Files.readAllLines(path);
            FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
            fileReadResultDTO.setLines(lines);
            fileReadResultDTO.setJavaFile(file.getName().endsWith(".java"));
            fileReadResultDTO.setReaded(true);
            return fileReadResultDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFile(String [] paths){
        String projectPath = applicatonState.getProjectPath();
        Path path = Path.of(projectPath, paths);
        File file = path.toFile();
        file.delete();
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
