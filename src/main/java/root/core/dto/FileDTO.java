package root.core.dto;

import java.nio.file.Path;

public class FileDTO {

    private Path path;


    public FileDTO(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

}
