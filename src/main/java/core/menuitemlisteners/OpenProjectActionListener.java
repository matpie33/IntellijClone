package core.menuitemlisteners;

import core.*;
import core.dto.FileDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;
import core.uibuilders.FileTreeBuilderUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

@Component
public class OpenProjectActionListener implements MenuItemListener {

    private JFileChooser jFileChooser;

    private ProjectStructureReader projectStructureReader;

    private FileTreeBuilderUI fileTreeBuilderUI;

    private UIEventsQueue uiEventsQueue;


    public OpenProjectActionListener(ProjectStructureReader projectStructureReader, FileTreeBuilderUI fileTreeBuilderUI, UIEventsQueue uiEventsQueue) {
        this.projectStructureReader = projectStructureReader;
        this.fileTreeBuilderUI = fileTreeBuilderUI;
        this.uiEventsQueue = uiEventsQueue;
        jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int action = jFileChooser.showOpenDialog(Main.FRAME);
        if (action == JFileChooser.APPROVE_OPTION){
            File selectedFile = jFileChooser.getSelectedFile();
            List<FileDTO> files = projectStructureReader.readProjectDirectory(selectedFile);
            JTree tree = fileTreeBuilderUI.build(selectedFile, files);
            uiEventsQueue.handleEvent(UIEventType.FILE_OPENED, tree);
        }
    }

    @Override
    public String getName() {
        return "Open";
    }
}
