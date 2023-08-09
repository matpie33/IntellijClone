package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileSystemChangeDTO;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class DirectoriesWatcher implements UIEventObserver {


    private ApplicatonState applicatonState;
    private WatchService watcher;

    public DirectoriesWatcher(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }


    private void watchDirectory (Path path) throws IOException {
        path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    public void watchProjectDirectoryForChanges(){
        File rootDirectory = applicatonState.getProjectPath();
        monitorPathsChanges(rootDirectory);
    }

    public void stopWatchingDirectories (){
        try {
            applicatonState.getFileWatcher().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void monitorPathsChanges(File selectedFile) {
        try {
            addPathsToWatchService(selectedFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addPathsToWatchService(File selectedFile) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        applicatonState.setFileWatcher(watcher);
        Files.walkFileTree(selectedFile.toPath(), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes)
                    throws IOException {
                directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case PROJECT_STRUCTURE_CHANGED:
                FileSystemChangeDTO fileSystemChangeDTO = (FileSystemChangeDTO) data;
                List<Path> createdFiles = fileSystemChangeDTO.getCreatedFiles();
                for (Path createdFile : createdFiles) {
                    if (createdFile.toFile().isDirectory()){
                        try {
                            watchDirectory(createdFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                for (Path modifiedFile : fileSystemChangeDTO.getModifiedFiles()) {
                    if (modifiedFile.toFile().isDirectory()){
                        try {
                            watchDirectory(modifiedFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        }
    }
}
