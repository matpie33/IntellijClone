package root.core.fileio;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassStructureParser;
import root.core.directory.changesdetecting.DirectoriesWatcher;
import root.core.dto.ApplicationState;
import root.core.dto.FileReadResultDTO;
import root.core.dto.ProjectStructureTreeElementDTO;
import root.core.dto.RenamedFileDTO;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileIO {

    private ApplicationState applicationState;

    private DirectoriesWatcher directoriesWatcher;

    private ClassStructureParser classStructureParser;

    private UIEventsQueue uiEventsQueue;

    public FileIO(ApplicationState applicationState, DirectoriesWatcher directoriesWatcher, ClassStructureParser classStructureParser, UIEventsQueue uiEventsQueue) {
        this.applicationState = applicationState;
        this.directoriesWatcher = directoriesWatcher;
        this.classStructureParser = classStructureParser;
        this.uiEventsQueue = uiEventsQueue;
    }

    public File getFile(ProjectStructureTreeElementDTO[] directories ){
        String projectPath = applicationState.getProjectPath().getParent();
        String [] paths = Arrays.stream(directories).map(ProjectStructureTreeElementDTO::getDisplayName).toArray(String[]::new);
        Path path = Path.of(projectPath, paths);
        return path.toFile();
    }

    public FileReadResultDTO read(String[] directories){
        String projectPath = applicationState.getProjectPath().getParent();
        Path path = Path.of(projectPath, directories);
        return readFile(path);
    }

    public FileReadResultDTO readFile(Path path){

        try {
            File file = path.toFile();
            if (!file.exists() || file.isDirectory()){
                return new FileReadResultDTO();
            }
            List<String> lines = Files.readAllLines(path);
            FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
            fileReadResultDTO.setContentLines(lines);
            fileReadResultDTO.setFile(file);
            fileReadResultDTO.setEditable(true);
            fileReadResultDTO.setJavaFile(file.getName().endsWith(".java"));
            fileReadResultDTO.setReadSuccessfully(true);
            fileReadResultDTO.setPathFromRoot(path.toString());
            return fileReadResultDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<String> getContent (Path path) throws IOException {
        return Files.readAllLines(path);
    }

    public void removeFile(ProjectStructureTreeElementDTO[] nodePaths){
        String[] nodeNames = Arrays.stream(nodePaths).map(ProjectStructureTreeElementDTO::getDisplayName).toArray(String[] ::new);
        String projectPath = applicationState.getProjectPath().getParent();
        Path path = Path.of(projectPath, nodeNames);
        File file = path.toFile();
        boolean isDeleted = file.delete();
        if (!isDeleted){
            System.out.println("is not deleted");
        }
    }


    public void save(String text){
        File openedFile = applicationState.getOpenedFile();
        if (openedFile == null || text==null){
            return;
        }
        try {
            Files.writeString(openedFile.toPath(), text);
            uiEventsQueue.dispatchEvent(UIEventType.AUTOSAVE_DONE, new Object());
            if (applicationState.getClassesWithCompilationErrors().contains(openedFile)){
                boolean isMain = classStructureParser.parseClassStructure(openedFile);
                if (isMain){
                    applicationState.addClassWithMainMethod(openedFile);
                    applicationState.removeClassWithCompilationError(openedFile);
                    uiEventsQueue.dispatchEvent(UIEventType.COMPILATION_ERROR_FIXED_IN_OPENED_FILE, new Object());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RenameResult renameFile (RenamedFileDTO renamedFileDTO){
        File file = renamedFileDTO.getFile();

        Map<File, File> replacements = getFilesThatNeedPathsUpdate(renamedFileDTO, file);
        File newFile = file.toPath().resolveSibling(renamedFileDTO.getNewName()).toFile();
        directoriesWatcher.stopWatchingDirectories();
        boolean isRenamed = file.renameTo(newFile);
        if (isRenamed){
            applicationState.updatePathsToClassesWithMainMethods(replacements);
            applicationState.renameFileIfContainsMainMethod(file, newFile);
        }
        directoriesWatcher.watchProjectDirectoryForChanges();
        return new RenameResult(newFile, isRenamed);
    }

    private Map<File, File> getFilesThatNeedPathsUpdate(RenamedFileDTO renamedFileDTO, File file) {
        Map<File, File> filesToReplace=  new HashMap<>();
        if (file.isDirectory()){
            String parentDirectory = file.getParentFile().getAbsolutePath();
            String directoryToRenamePath = file.getAbsolutePath();
            List<File> classesWithMainMethod = applicationState.getClassesWithMainMethod();
            for (File mainMethodClass : classesWithMainMethod) {
                if (mainMethodClass.getAbsolutePath().startsWith(directoryToRenamePath)){
                    String oldPath = mainMethodClass.getAbsolutePath();
                    String newPath = oldPath.replace(directoryToRenamePath, parentDirectory+File.separator+ renamedFileDTO.getNewName());
                    Path updatedPath = Path.of(newPath);
                    filesToReplace.put(mainMethodClass, updatedPath.toFile());
                }
            }
        }
        return filesToReplace;
    }

    public class RenameResult {
        private File newFile;
        private boolean isSuccess;

        public RenameResult(File newFile, boolean isSuccess) {
            this.newFile = newFile;
            this.isSuccess = isSuccess;
        }


        public File getNewFile() {
            return newFile;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }


}
