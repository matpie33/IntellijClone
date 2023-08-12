package root.core.shortcuts;

import org.springframework.stereotype.Component;
import root.core.context.actionlisteners.FileDeleteRequestListener;
import root.core.context.actionlisteners.FileRenameListener;
import root.core.context.actionlisteners.KeyPressListener;
import root.core.dto.ShortcutDTO;
import root.core.nodehandling.ProjectStructureNodesHandler;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectStructureTreeShortcuts {

    private  List<ShortcutDTO> shortcutsList = new ArrayList<>();

    private FileDeleteRequestListener fileDeleteRequestListener;

    private FileRenameListener fileRenameListener;

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private ShortcutAssigner shortcutAssigner;

    public ProjectStructureTreeShortcuts(FileDeleteRequestListener fileDeleteRequestListener, FileRenameListener fileRenameListener, ProjectStructureNodesHandler projectStructureNodesHandler, ShortcutAssigner shortcutAssigner) {
        this.fileDeleteRequestListener = fileDeleteRequestListener;
        this.fileRenameListener = fileRenameListener;
        this.shortcutAssigner = shortcutAssigner;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
    }

    @PostConstruct
    public void init (){
        shortcutsList.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete",new KeyPressListener<>(fileDeleteRequestListener, projectStructureNodesHandler)));
        shortcutsList.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename",new KeyPressListener<> (fileRenameListener, projectStructureNodesHandler)));
    }

    public void assignShortcuts(JComponent component){
        shortcutsList.forEach(shortcut->shortcutAssigner.assignShortcut(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }

}
