package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;
import root.core.dto.JDKPathValidationDTO;
import root.core.uievents.UIViewUpdater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@Component
public class JDKPathBrowseActionListener implements ActionListener {

    private JFileChooser jdkPathChooser;
    private JDKPathValidator jdkPathValidator;
    private boolean isCorrectJDK;

    private UIViewUpdater uiViewUpdater;

    public JDKPathBrowseActionListener (JDKPathValidator jdkPathValidator){
        this.jdkPathValidator = jdkPathValidator;
        jdkPathChooser = new JFileChooser();
        jdkPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    public boolean isCorrectJDK() {
        return isCorrectJDK;
    }

    public void setViewUpdater (UIViewUpdater uiViewUpdater){
        this.uiViewUpdater= uiViewUpdater;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = jdkPathChooser.showOpenDialog(uiViewUpdater.getDialog());
        if (result == JFileChooser.APPROVE_OPTION){
            File selectedDirectory = jdkPathChooser.getSelectedFile();
            boolean pathValid = jdkPathValidator.isPathValid(selectedDirectory);
            JDKPathValidationDTO pathValidationDTO = new JDKPathValidationDTO(selectedDirectory, pathValid);
            uiViewUpdater.updateNeeded(pathValidationDTO);
            isCorrectJDK = pathValid;
        }
    }
}
