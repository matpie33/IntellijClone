package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;
import root.core.uievents.UIViewUpdater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@Component
public class JDKPathBrowseActionListener implements ActionListener {

    private JFileChooser jdkPathChooser;


    private UIViewUpdater uiViewUpdater;

    public JDKPathBrowseActionListener (){
        jdkPathChooser = new JFileChooser();
        jdkPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }


    public void setViewUpdater (UIViewUpdater uiViewUpdater){
        this.uiViewUpdater= uiViewUpdater;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = jdkPathChooser.showOpenDialog(uiViewUpdater.getDialog());
        if (result == JFileChooser.APPROVE_OPTION){
            File selectedDirectory = jdkPathChooser.getSelectedFile();
            uiViewUpdater.updateNeeded(selectedDirectory);
        }
    }
}
