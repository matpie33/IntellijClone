package core.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApplicatonState {


    private List<File> classesWithMainMethod = new ArrayList<>();

    private WatchService fileWatcher;

    private String projectPath;

    private String projectRootDirectoryName;

    private File openedFile;

    public void addClassWithMainMethod (File file){
        classesWithMainMethod.add(file);
    }

    public List<File> getClassesWithMainMethod() {
        return classesWithMainMethod;
    }

    public void renameFileIfContainsMainMethod(File oldFile, File newFile ){
        if (classesWithMainMethod.contains(oldFile)){
            classesWithMainMethod.remove(oldFile);
            classesWithMainMethod.add(newFile);
        }
    }

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

    public void updatePathsToClassesWithMainMethods(Map<File, File> replacements) {
        for (Map.Entry<File, File> replacement : replacements.entrySet()) {
            classesWithMainMethod.remove(replacement.getKey());
            classesWithMainMethod.add(replacement.getValue());
        }
    }
}
