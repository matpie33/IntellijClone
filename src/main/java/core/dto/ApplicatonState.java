package core.dto;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.WatchService;
import java.util.*;

@Component
public class ApplicatonState {


    private Map<File, ClassStructureDTO> classStructureDTOs = new HashMap<>();

    private List<File> classesWithMainMethod = new ArrayList<>();

    private Set<File> classesToRecompile = new HashSet<>();

    private List<String> availableClassNames = new ArrayList<>();

    private Multimap<String, String> classNameToPackageMap = ArrayListMultimap.create();


    private WatchService fileWatcher;

    private File projectPath;

    private File openedFile;

    private String classPath;

    private String outputDirectory;

    private Set<Process> runningProcesses = new HashSet<>();

    private Set<File> classesWithCompilationErrors = new HashSet<>();

    private String localRepositoryPath;

    public void addClassWithPackage (String className, String packageName){
        availableClassNames.add(className);
        classNameToPackageMap.put(className, packageName);
    }

    public Collection<String> getPackageNamesForClass(String className){
        return classNameToPackageMap.get(className);
    }

    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }


    public ClassStructureDTO getClassStructureOfOpenedFile(){
        return classStructureDTOs.get(getOpenedFile());
    }

    public void putClassStructure (File file, ClassStructureDTO classStructure){
        classStructureDTOs.put(file, classStructure);
    }

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

    public List<String> getAvailableClassNames() {
        return availableClassNames;
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

    public void addClassWithCompilationError (File file){
        classesWithCompilationErrors.add(file);
    }

    public void removeClassWithCompilationError (File file){
        classesWithCompilationErrors.remove(file);
    }

    public Set<File> getClassesWithCompilationErrors() {
        return classesWithCompilationErrors;
    }
}
