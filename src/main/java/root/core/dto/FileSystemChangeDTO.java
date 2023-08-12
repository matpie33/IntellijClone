package root.core.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSystemChangeDTO {

    private List<Path> createdFiles;
    private List<Path> modifiedFiles;
    private List<Path> deletedFiles;

    public FileSystemChangeDTO(List<Path> createdFiles, List<Path> modifiedFiles, List<Path> deletedFiles) {
        this.createdFiles = createdFiles;
        this.modifiedFiles = modifiedFiles;
        this.deletedFiles = deletedFiles;
    }

    public List<Path> getCreatedFiles() {
        return new ArrayList<>(createdFiles);
    }

    public List<Path> getModifiedFiles() {
        return new ArrayList<>(modifiedFiles);
    }

    public List<Path> getDeletedFiles() {
        return new ArrayList<>(deletedFiles);
    }
}
