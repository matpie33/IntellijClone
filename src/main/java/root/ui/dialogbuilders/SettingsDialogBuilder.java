package root.ui.dialogbuilders;

import org.springframework.stereotype.Component;
import root.core.configuration.ConfigurationHolder;
import root.core.configuration.ConfigurationHolderType;
import root.core.jdk.manipulating.JDKPathBrowseActionListener;
import root.core.jdk.manipulating.JDKPathValidator;
import root.core.shortcuts.DialogShortcuts;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.core.uievents.UIViewUpdater;
import root.core.utility.UIUtilities;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

@Component
public class SettingsDialogBuilder implements UIViewUpdater, UIEventObserver {

    public static final String TITLE = "Settings";
    private JDialog dialog;
    private JTextField jdkInputField;

    private ConfigurationHolder configurationHolder;

    private JLabel errorLabel;

    private JDKPathBrowseActionListener jdkPathBrowseActionListener;

    private DialogShortcuts dialogShortcuts;

    private JDKPathValidator jdkPathValidator;

    public SettingsDialogBuilder(ConfigurationHolder ConfigurationHolder, JDKPathBrowseActionListener jdkPathBrowseActionListener, DialogShortcuts dialogShortcuts, JDKPathValidator jdkPathValidator) {
        this.configurationHolder = ConfigurationHolder;
        this.jdkPathBrowseActionListener = jdkPathBrowseActionListener;
        this.dialogShortcuts = dialogShortcuts;
        this.jdkPathValidator = jdkPathValidator;
        jdkPathBrowseActionListener.setViewUpdater(this);
    }

    @PostConstruct
    private void init() {

        initDialog();
        JPanel panel = initContentPane();
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialogShortcuts.assignShortcuts(panel);

    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case DIALOG_CLOSE_REQUEST:
                if (dialog.isVisible()){
                    jdkInputField.setText("");
                    dialog.dispose();
                }
                break;
            case DIALOG_ACCEPT_REQUEST:
                if (dialog.isVisible() ){
                    validateAndSave();
                }
                break;
        }
    }

    @Override
    public void updateNeeded(Object data) {
        File selectedDirectory = (File) data;
        jdkInputField.setText(selectedDirectory.toString());
        jdkPathValidator.validate(selectedDirectory);
        if (!jdkPathValidator.isValid()){
            errorLabel.setText("JDK path is incorrect.");
            errorLabel.setForeground(Color.RED);
        }
        else{
            errorLabel.setText("JDK path is correct");
            errorLabel.setForeground(Color.GREEN);
        }

    }

    @Override
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
        saveButton.addActionListener(e -> validateAndSave());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e->cancel());
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void cancel() {
        dialog.dispose();
    }

    private void validateAndSave() {
        jdkPathValidator.validate(Path.of(jdkInputField.getText()).toFile());
        boolean isJdkPathValid = jdkPathValidator.isValid();
        if (isJdkPathValid){
            configurationHolder.saveConfiguration(jdkInputField.getText(), ConfigurationHolderType.JDK);
            dialog.dispose();
        }
        else{
            errorLabel.setText("Wrong jdk path.");
            errorLabel.setForeground(Color.RED);
        }
    }

    private JPanel getOptionsConfigurationPanel() {
        JPanel optionsConfigurationPanel = new JPanel();
        optionsConfigurationPanel.setLayout(new BoxLayout(optionsConfigurationPanel, BoxLayout.PAGE_AXIS));

        JLabel jdkPathLabel = new JLabel("JDK path: ");
        jdkInputField = new JTextField(configurationHolder.getJdkPath(), 30);
        JButton browseButton = new JButton("browse");
        browseButton.addActionListener(jdkPathBrowseActionListener);
        errorLabel = new JLabel("Select jdk path");

        JPanel jdkPathPanel = UIUtilities.addElementsAsSingleLine(jdkPathLabel, jdkInputField, browseButton);
        optionsConfigurationPanel.add(jdkPathPanel);
        optionsConfigurationPanel.add(errorLabel);
        return optionsConfigurationPanel;
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

    public void focusInputField() {
        jdkInputField.requestFocusInWindow();
    }
}
