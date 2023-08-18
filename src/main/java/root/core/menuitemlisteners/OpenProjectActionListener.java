package root.core.menuitemlisteners;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.configuration.ConfigurationHolder;
import root.core.configuration.ConfigurationHolderType;
import root.core.projectmanipulating.ProjectOpener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

@Component
public class OpenProjectActionListener implements MenuItemListener {


    private JFileChooser jFileChooser;

    private ProjectOpener projectOpener;
    private ConfigurationHolder configurationHolder;

    public OpenProjectActionListener(ProjectOpener projectOpener, ConfigurationHolder configurationHolder) {
        this.configurationHolder = configurationHolder;
        jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        this.projectOpener = projectOpener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int action = jFileChooser.showOpenDialog(Main.FRAME);
        if (action == JFileChooser.APPROVE_OPTION){
            File rootDirectory = jFileChooser.getSelectedFile();
            configurationHolder.saveConfiguration(rootDirectory.toString(), ConfigurationHolderType.RECENT_PROJECTS);
            projectOpener.openProject(rootDirectory);
        }
    }




    @Override
    public String getName() {
        return "Open";
    }
}
