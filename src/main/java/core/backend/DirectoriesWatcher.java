package core.backend;

import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class DirectoriesWatcher {


    private ApplicatonState applicatonState;

    public DirectoriesWatcher(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public void watchProjectDirectory (){
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
        WatchService watcher = FileSystems.getDefault().newWatchService();
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

}
