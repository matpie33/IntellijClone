package core.dto;

import java.io.File;

public class RenamedFileDTO {

    private File file;

    private String newName;

    public RenamedFileDTO(File file, String newName) {
        this.file = file;
        this.newName = newName;
    }

    public File getFile() {
        return file;
    }

    public String getNewName() {
        return newName;
    }
}
