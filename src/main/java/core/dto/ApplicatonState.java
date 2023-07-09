package core.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicatonState {



    private WatchService fileWatcher;

    private String projectPath;

    private String projectRootDirectoryName;

    private File openedFile;


    public String getProjectRootDirectoryName() {
        return projectRootDirectoryName;
    }

    public void setProjectRootDirectoryName(String projectRootDirectoryName) {
        this.projectRootDirectoryName = projectRootDirectoryName;
    }


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
