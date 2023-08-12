package root.core.directory.changesdetecting;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.FileSystemChangeDTO;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class DirectoriesWatcher implements UIEventObserver {

    private ApplicationState applicationState;
    private WatchService watcher;

    public DirectoriesWatcher(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }


    private void watchDirectory (Path path) throws IOException {
        path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    public void watchProjectDirectoryForChanges(){
        File rootDirectory = applicationState.getProjectPath();
        watchDirectoriesInRootDirectory(rootDirectory);
    }

    public void stopWatchingDirectories (){
        try {
            applicationState.getFileWatcher().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void watchDirectoriesInRootDirectory(File rootDirectory) {
        try {
            addPathsToWatchService(rootDirectory);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addPathsToWatchService(File selectedFile) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        applicationState.setFileWatcher(watcher);
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
                List<Path> modifiedFiles = fileSystemChangeDTO.getModifiedFiles();
                watchDirectories(createdFiles);
                watchDirectories(modifiedFiles);
        }
    }

    private void watchDirectories(List<Path> filesList) {
        for (Path filePath : filesList) {
            if (filePath.toFile().isDirectory()){
                try {
                    watchDirectory(filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
