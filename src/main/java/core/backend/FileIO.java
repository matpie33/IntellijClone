package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.dto.RenamedFileDTO;
import core.dto.TreeNodeFileDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

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

    private ApplicatonState applicatonState;

    private DirectoriesWatcher directoriesWatcher;

    private ClassStructureParser classStructureParser;

    private UIEventsQueue uiEventsQueue;

    public FileIO(ApplicatonState applicatonState, DirectoriesWatcher directoriesWatcher, ClassStructureParser classStructureParser, UIEventsQueue uiEventsQueue) {
        this.applicatonState = applicatonState;
        this.directoriesWatcher = directoriesWatcher;
        this.classStructureParser = classStructureParser;
        this.uiEventsQueue = uiEventsQueue;
    }

    public File getFile(TreeNodeFileDTO[] directories ){
        String projectPath = applicatonState.getProjectPath().getParent();
        String [] paths = Arrays.stream(directories).map(TreeNodeFileDTO::getDisplayName).toArray(String[]::new);
        Path path = Path.of(projectPath, paths);
        return path.toFile();
    }

    public FileReadResultDTO read(String[] directories){
        String projectPath = applicatonState.getProjectPath().getParent();
        Path path = Path.of(projectPath, directories);
        return readFile(path);
    }

    public FileReadResultDTO readFile(Path path){

        try {
            String projectPath = applicatonState.getProjectPath().getParent();

            File file = path.toFile();
            if (!file.exists() || file.isDirectory()){
                return new FileReadResultDTO();
            }
            applicatonState.setOpenedFile(file);
            List<String> lines = Files.readAllLines(path);
            FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
            fileReadResultDTO.setLines(lines);
            fileReadResultDTO.setFile(file);
            fileReadResultDTO.setJavaFile(file.getName().endsWith(".java"));
            fileReadResultDTO.setReaded(true);
            fileReadResultDTO.setPathFromRoot(Path.of(projectPath).relativize(path).toString());
            return fileReadResultDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<String> getContent (Path path) throws IOException {
        return Files.readAllLines(path);
    }

    public void removeFile(TreeNodeFileDTO [] nodePaths){
        String[] nodeNames = Arrays.stream(nodePaths).map(TreeNodeFileDTO::getDisplayName).toArray(String[] ::new);
        String projectPath = applicatonState.getProjectPath().getParent();
        Path path = Path.of(projectPath, nodeNames);
        File file = path.toFile();
        boolean isDeleted = file.delete();
        if (!isDeleted){
            System.out.println("is not deleted");
        }
    }


    public void save(String text){
        File openedFile = applicatonState.getOpenedFile();
        if (openedFile == null || text==null){
            return;
        }
        try {
            Files.writeString(openedFile.toPath(), text);
            if (applicatonState.getClassesWithCompilationErrors().contains(openedFile)){
                boolean isMain = classStructureParser.parseClassStructure(openedFile);
                if (isMain){
                    applicatonState.addClassWithMainMethod(openedFile);
                    applicatonState.removeClassWithCompilationError(openedFile);
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
            applicatonState.updatePathsToClassesWithMainMethods(replacements);
            applicatonState.renameFileIfContainsMainMethod(file, newFile);
        }
        directoriesWatcher.watchProjectDirectoryForChanges();
        return new RenameResult(newFile, isRenamed);
    }

    private Map<File, File> getFilesThatNeedPathsUpdate(RenamedFileDTO renamedFileDTO, File file) {
        Map<File, File> filesToReplace=  new HashMap<>();
        if (file.isDirectory()){
            String parentDirectory = file.getParentFile().getAbsolutePath();
            String directoryToRenamePath = file.getAbsolutePath();
            List<File> classesWithMainMethod = applicatonState.getClassesWithMainMethod();
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
