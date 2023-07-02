package core.dto;

import java.util.List;

public class DirectoryDTO extends FileDTO{

    private List<FileDTO> files;

    public DirectoryDTO(String name, String absolutePath) {
        super(name, absolutePath);
    }

    public void setFiles(List<FileDTO> files) {
        this.files = files;
    }

    public List<FileDTO> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return " Directory " + super.toString() +
                "files=" + files +
                '}';
    }
}
