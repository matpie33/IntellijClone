package core.backend;

import core.dto.ApplicatonState;
import core.dto.FileSystemChangeDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DirectoryChangesDetector extends MouseAdapter implements WindowFocusListener {

    private ApplicatonState applicatonState;

    private UIEventsQueue uiEventsQueue;

    public DirectoryChangesDetector(ApplicatonState applicatonState, UIEventsQueue uiEventsQueue) {
        this.applicatonState = applicatonState;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkDirectory();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        checkDirectory();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {

    }

    private void checkDirectory (){
        if (applicatonState.getFileWatcher() == null){
            return;
        }
        WatchKey key = applicatonState.getFileWatcher().poll();
        if (key != null){
            List<WatchEvent<?>> watchEvents = key.pollEvents();
            List<Path> createdFiles = new ArrayList<>();
            List<Path> removedFiles = new ArrayList<>();
            List<Path> modifiedFiles = new ArrayList<>();
            for (WatchEvent<?> watchEvent : watchEvents) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                Path file = (Path) watchEvent.context();
                Path directory = (Path) key.watchable();
                Path absolutePath = directory.resolve(file);
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

            FileSystemChangeDTO fileSystemChangeDTO = new FileSystemChangeDTO(createdFiles, modifiedFiles, removedFiles);
            uiEventsQueue.dispatchEvent(UIEventType.PROJECT_STRUCTURE_CHANGED, fileSystemChangeDTO);
            key.reset();
        }
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
