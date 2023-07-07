package core.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.WatchService;

@Component
public class ApplicatonState {



    private WatchService fileWatcher;

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

    public WatchService getFileWatcher() {
        return fileWatcher;
    }

    public void setFileWatcher(WatchService fileWatcher) {
        this.fileWatcher = fileWatcher;
    }

}
