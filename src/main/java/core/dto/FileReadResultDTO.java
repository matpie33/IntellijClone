package core.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileReadResultDTO {

    private String pathFromRoot;

    private List<String> lines =new ArrayList<>();

    private File file;

    private boolean isJavaFile;

    private boolean isReaded;

    public String getPathFromRoot() {
        return pathFromRoot;
    }

    public void setPathFromRoot(String pathFromRoot) {
        this.pathFromRoot = pathFromRoot;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public void setReaded(boolean readed) {
        isReaded = readed;
    }

    public boolean isJavaFile() {
        return isJavaFile;
    }

    public void setJavaFile(boolean javaFile) {
        isJavaFile = javaFile;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
