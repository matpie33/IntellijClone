package core.shortcuts;

import core.backend.ShortcutAssigner;
import core.context.actionlisteners.*;
import core.context.providers.NodePathManipulation;
import core.dto.ShortcutDTO;
import org.springframework.stereotype.Component;

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

    private NodePathManipulation nodePathManipulation;

    private ShortcutAssigner shortcutAssigner;

    public ProjectStructureTreeShortcuts(FileDeleteRequestListener fileDeleteRequestListener, FileRenameListener fileRenameListener, NodePathManipulation nodePathManipulation, ShortcutAssigner shortcutAssigner) {
        this.fileDeleteRequestListener = fileDeleteRequestListener;
        this.fileRenameListener = fileRenameListener;
        this.nodePathManipulation = nodePathManipulation;
        this.shortcutAssigner = shortcutAssigner;
    }

    @PostConstruct
    public void init (){
        shortcutsList.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete",new KeyPressListener<>(fileDeleteRequestListener, nodePathManipulation)));
        shortcutsList.add(new ShortcutDTO(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename",new KeyPressListener<> (fileRenameListener, nodePathManipulation )));
    }

    public void assignShortcuts(JComponent component){
        shortcutsList.forEach(shortcut->shortcutAssigner.assignShortcut(component, shortcut.getKeyStroke(), shortcut.getActionName(), shortcut.getAction()));
    }

}
