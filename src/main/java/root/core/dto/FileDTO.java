package root.core.dto;

import java.nio.file.Path;

public class FileDTO {

    private final boolean isDirectory;
    private Path path;


    public FileDTO(Path path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
