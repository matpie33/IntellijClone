package root.core.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileReadResultDTO {

    private String pathFromRoot;

    private List<String> contentLines =new ArrayList<>();

    private File file;

    private boolean isJavaFile;

    private boolean isReadSuccessfully;

    private boolean isEditable;

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public String getPathFromRoot() {
        return pathFromRoot;
    }

    public void setPathFromRoot(String pathFromRoot) {
        this.pathFromRoot = pathFromRoot;
    }

    public boolean isReadSuccessfully() {
        return isReadSuccessfully;
    }

    public void setReadSuccessfully(boolean readSuccessfully) {
        isReadSuccessfully = readSuccessfully;
    }

    public boolean isJavaFile() {
        return isJavaFile;
    }

    public void setJavaFile(boolean javaFile) {
        isJavaFile = javaFile;
    }

    public List<String> getContentLines() {
        return contentLines;
    }

    public void setContentLines(List<String> contentLines) {
        this.contentLines = contentLines;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
