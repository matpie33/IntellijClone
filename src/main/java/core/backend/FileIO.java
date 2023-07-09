package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.dto.RenamedFileDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileIO {

    private ApplicatonState applicatonState;

    private DirectoriesWatcher directoriesWatcher;

    public FileIO(ApplicatonState applicatonState, DirectoriesWatcher directoriesWatcher) {
        this.applicatonState = applicatonState;
        this.directoriesWatcher = directoriesWatcher;
    }

    public File getFile(String[] directories ){
        String projectPath = applicatonState.getProjectPath();
        Path path = Path.of(projectPath, directories);
        return path.toFile();
    }

    public FileReadResultDTO read(String[] directories){
        String projectPath = applicatonState.getProjectPath();
        try {
            Path path = Path.of(projectPath, directories);
            File file = path.toFile();
            if (file.isDirectory()){
                FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
                return fileReadResultDTO;
            }
            applicatonState.setOpenedFile(file);
            List<String> lines = Files.readAllLines(path);
            FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
            fileReadResultDTO.setLines(lines);
            fileReadResultDTO.setJavaFile(file.getName().endsWith(".java"));
            fileReadResultDTO.setReaded(true);
            return fileReadResultDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFile(String [] paths){
        String projectPath = applicatonState.getProjectPath();
        Path path = Path.of(projectPath, paths);
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
        directoriesWatcher.watchProjectDirectory();
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
