package core.dto;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.WatchService;
import java.util.*;

@Component
public class ApplicatonState {


    private List<File> classesWithMainMethod = new ArrayList<>();

    private Set<File> classesToRecompile = new HashSet<>();

    private WatchService fileWatcher;

    private File projectPath;

    private File openedFile;

    private String classPath;

    private String outputDirectory;

    private Set<Process> runningProcesses = new HashSet<>();

    public void addRunningProcess (Process process){
        runningProcesses.add(process);
    }

    public void removeRunningProcess (Process process){
        runningProcesses.remove(process);
    }

    public Set<Process> getRunningProcesses() {
        return runningProcesses;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Set<File> getClassesToRecompile() {
        return classesToRecompile;
    }

    public void clearClassesToRecompile (){
        classesToRecompile.clear();
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

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

    public File getOpenedFile() {
        return openedFile;
    }

    public void setOpenedFile(File openedFile) {
        this.openedFile = openedFile;
    }

    public File getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(File projectPath) {
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

    public void addCurrentFileToClassesToRecompile() {
        classesToRecompile.add(openedFile);
    }
}
