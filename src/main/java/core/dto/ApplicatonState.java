package core.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class ApplicatonState {


    private String projectPath;

    private File openedFile;


    public File getOpenedFile() {
        return openedFile;
    }

    public void setOpenedFile(File openedFile) {
        this.openedFile = openedFile;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
}
