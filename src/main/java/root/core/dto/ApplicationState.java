package root.core.dto;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;

import java.io.File;
import java.nio.file.WatchService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApplicationState {


    private final Map<File, ClassStructureDTO> classStructureDTOs = new ConcurrentHashMap<>();

    private final List<File> classesWithMainMethod = new ArrayList<>();

    private final Set<File> classesToRecompile = new HashSet<>();


    private final Multimap<String, ClassNavigationDTO> classNameToInfoDTO = ArrayListMultimap.create();

    private final Object classNamesLock = new Object();


    private WatchService fileWatcher;

    private File projectPath;

    private File openedFile;

    private String classPath;

    private String buildOutputDirectory;

    private final Set<Process> runningProcesses = new HashSet<>();

    private final Set<File> classesWithCompilationErrors = new HashSet<>();

    private String localRepositoryPath;

    public void addClassWithPackage (String className, String packageName, ClassOrigin origin, String rootDirectory){
        ClassNavigationDTO classNavigationDTO = new ClassNavigationDTO(rootDirectory, packageName, origin, className);
        synchronized (classNamesLock){
            classNameToInfoDTO.put(className, classNavigationDTO);
        }
    }

    public Object getClassNamesLock() {
        return classNamesLock;
    }

    public Collection<ClassNavigationDTO> getPackageNamesForClass(String className){
        return classNameToInfoDTO.get(className);
    }

    public synchronized Collection<ClassNavigationDTO> getAvailableClassNames (){
        return classNameToInfoDTO.values();
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

    public ClassStructureDTO getClassStructure(File file){
        return classStructureDTOs.get(file);
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

    public String getBuildOutputDirectory() {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory(String buildOutputDirectory) {
        this.buildOutputDirectory = buildOutputDirectory;
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
        return Optional.ofNullable(openedFile).orElse(new File("temp"));
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

    public void addClassWithPackage(String fullName, String pathToJar, ClassOrigin origin) {
        if (!fullName.contains(".")){
            return;
        }
        String replacedDollarSigns = fullName.replace("$", ".");

        int lastDot = replacedDollarSigns.lastIndexOf('.');
        String className = replacedDollarSigns.substring(lastDot+1);
        String packageName = replacedDollarSigns.substring(0, lastDot);
        addClassWithPackage(className, packageName, origin, pathToJar);
    }
}
