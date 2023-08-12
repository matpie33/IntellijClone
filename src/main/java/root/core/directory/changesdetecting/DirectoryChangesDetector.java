package root.core.directory.changesdetecting;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.FileSystemChangeDTO;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.List;

@Component
public class DirectoryChangesDetector extends MouseAdapter implements WindowFocusListener {

    private ApplicationState applicationState;

    private UIEventsQueue uiEventsQueue;

    public DirectoryChangesDetector(ApplicationState applicationState, UIEventsQueue uiEventsQueue) {
        this.applicationState = applicationState;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkForChangesInWatchedDirectories();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        checkForChangesInWatchedDirectories();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {

    }

    public void checkForChangesInWatchedDirectories(){
        if (applicationState.getFileWatcher() == null){
            return;
        }

        boolean shouldCheckForKeys = true;
        boolean changesDetected = false;
        List<Path> createdFiles = new ArrayList<>();
        List<Path> removedFiles = new ArrayList<>();
        List<Path> modifiedFiles = new ArrayList<>();
        while (shouldCheckForKeys){
            WatchKey key = applicationState.getFileWatcher().poll();
            if (key != null){
                changesDetected = true;
                List<WatchEvent<?>> watchEvents = key.pollEvents();
                for (WatchEvent<?> watchEvent : watchEvents) {
                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    Path absolutePath = getFilePathFromEvent(key, watchEvent);
                    handleFileChangeEvent(createdFiles, removedFiles, modifiedFiles, kind, absolutePath);
                }

                key.reset();
            }
            shouldCheckForKeys = key !=null;
        }
        if (changesDetected){
            FileSystemChangeDTO fileSystemChangeDTO = new FileSystemChangeDTO(createdFiles, modifiedFiles, removedFiles);
            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_STRUCTURE_CHANGED, fileSystemChangeDTO);
        }


    }

    private void handleFileChangeEvent(List<Path> createdFiles, List<Path> removedFiles, List<Path> modifiedFiles, WatchEvent.Kind<?> kind, Path absolutePath) {
        if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)){
            handleFileDelete(createdFiles, removedFiles, modifiedFiles, absolutePath);
        }
        else if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)){
            handleFileCreate(createdFiles, removedFiles, absolutePath);
        }
        else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)){
            modifiedFiles.add(absolutePath);
        }
    }

    private Path getFilePathFromEvent(WatchKey key, WatchEvent<?> watchEvent) {
        Path file = (Path) watchEvent.context();
        Path directory = (Path) key.watchable();
        Path absolutePath = directory.resolve(file);
        return absolutePath;
    }

    private void handleFileCreate(List<Path> createdFile, List<Path> removedFiles, Path file) {
        if (removedFiles.contains(file)){
            removedFiles.remove(file);
        }
        else{
            createdFile.add(file);

        }
    }

    private void handleFileDelete(List<Path> createdFiles, List<Path> removedFiles, List<Path> modifiedFiles, Path file) {
        if (createdFiles.contains(file) && modifiedFiles.contains(file)){
            createdFiles.remove(file);
            modifiedFiles.remove(file);
        }
        else if (createdFiles.contains(file)){
            createdFiles.remove(file);
        }
        else if (modifiedFiles.contains(file)){
            modifiedFiles.remove(file);
            removedFiles.add(file);
        }
        else{
            removedFiles.add(file);
        }
    }

}
