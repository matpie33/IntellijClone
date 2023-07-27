package core.dialogbuilders;

import core.backend.ConfigurationHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;

@Component
public class SettingsDialogBuilder {

    public static final String TITLE = "Settings";
    private JDialog dialog;
    private JFileChooser jdkPathChooser;
    private JTextField jdkInputField;

    private ConfigurationHolder configurationHolder;

    public SettingsDialogBuilder(ConfigurationHolder configurationHolder) {
        this.configurationHolder = configurationHolder;
    }

    @PostConstruct
    private void init() {
        jdkPathChooser = new JFileChooser();
        jdkPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        initDialog();
        JPanel panel = initContentPane();
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

    public JDialog getDialog (){
        return dialog;
    }

    private void initDialog() {
        dialog = new JDialog();
        dialog.setTitle(TITLE);
        dialog.setModal(true);
        dialog.setVisible(false);
    }

    private JPanel initContentPane() {
        JPanel contentPane = new JPanel();
        JPanel optionNamesPanel = getOptionNamesPanel();
        JPanel configurationPanel = getOptionsConfigurationPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionNamesPanel, configurationPanel);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);
        JPanel buttonsPanel = createButtonsPanel();
        contentPane.add(buttonsPanel, BorderLayout.PAGE_END);
        return contentPane;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = (FlowLayout) panel.getLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveOptions());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e->cancel());
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void cancel() {
        dialog.dispose();
    }

    private void saveOptions() {
        configurationHolder.saveConfiguration(jdkInputField.getText());
        dialog.dispose();
    }

    private JPanel getOptionsConfigurationPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("JDK path: "));
        jdkInputField = new JTextField(configurationHolder.getJdkPath(), 30);
        panel.add(jdkInputField);
        JButton browseButton = new JButton("browse");
        panel.add(browseButton);
        browseButton.addActionListener(e->{

            int result = jdkPathChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION){
                File selectedFile = jdkPathChooser.getSelectedFile();
                jdkInputField.setText(selectedFile.toString());
            }

        });
        return panel;
    }

    private JPanel getOptionNamesPanel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Options");
        rootNode.add(new DefaultMutableTreeNode("JDK path"));
        JTree tree = new JTree(rootNode);
        tree.expandRow(0);
        tree.setRootVisible(false);
        JPanel panel = new JPanel();
        panel.add(tree);
        return panel;



    }

}
