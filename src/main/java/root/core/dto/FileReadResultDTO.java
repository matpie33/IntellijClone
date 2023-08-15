package root.core.dto;

import root.core.classmanipulating.ClassOrigin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileReadResultDTO {

    private String pathFromRoot;

    private List<String> contentLines =new ArrayList<>();

    private File file;
    private boolean isReadSuccessfully;

    private ClassOrigin classOrigin;

    public ClassOrigin getClassOrigin() {
        return classOrigin;
    }

    public void setClassOrigin(ClassOrigin classOrigin) {
        this.classOrigin = classOrigin;
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
