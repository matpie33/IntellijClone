package root.ui.dialogbuilders;

import org.springframework.stereotype.Component;
import root.core.dto.RenamedFileDTO;
import root.core.shortcuts.DialogShortcuts;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

@Component
public class RenameFileDialogBuilder implements UIEventObserver {

    private JDialog dialog;

    private UIEventsQueue uiEventsQueue;
    private File file;
    private JTextField textInput;

    private DialogShortcuts dialogShortcuts;
    private DefaultMutableTreeNode treeNode;

    public RenameFileDialogBuilder(UIEventsQueue uiEventsQueue, DialogShortcuts dialogShortcuts) {
        this.uiEventsQueue = uiEventsQueue;
        this.dialogShortcuts = dialogShortcuts;
    }

    @PostConstruct
    public void init (){
        JPanel panel = initPanel();
        addComponents(panel);
        initDialog(panel);

    }

    public void showDialog(Point position, File file, DefaultMutableTreeNode treeNode) {
        this.file = file;
        this.treeNode = treeNode;
        textInput.setText(file.getName());
        dialog.setLocation(position.x, position.y);
        dialog.setVisible(true);
    }


    private JPanel initPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel;
    }

    private void initDialog(JPanel panel) {
        dialog = new JDialog();
        dialog.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                dialog.dispose();
            }
        });
        JRootPane rootPane = dialog.getRootPane();
        dialogShortcuts.assignShortcuts(rootPane);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(false);
    }

    private void addComponents (JPanel panel){
        JLabel header = createHeader();
        JTextField textField = createTextField();
        JButton accept = createButtonAccept();
        JButton cancel = createButtonCancel();

        panel.add(header);
        panel.add(textField);

        JPanel buttons = new JPanel();
        buttons.add(accept);
        buttons.add(cancel);

        panel.add(buttons);

    }

    private JButton createButtonCancel() {
        JButton button = new JButton("Cancel");
        button.addActionListener(e->dialog.dispose());
        return button;
    }

    private JButton createButtonAccept() {
        JButton button = new JButton("Rename");
        button.addActionListener(e-> acceptFilenameChange());
        return button;
    }

    private void acceptFilenameChange() {
        dialog.dispose();
        RenamedFileDTO renamedFileDTO = new RenamedFileDTO(file, textInput.getText(), treeNode);
        uiEventsQueue.dispatchEvent(UIEventType.FILENAME_CHANGED, renamedFileDTO);
        textInput.setText("");
    }

    private JTextField createTextField() {
        textInput = new JTextField(20);
        return textInput;
    }

    private JLabel createHeader() {
        JLabel label = new JLabel();
        label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        label.setText("Rename file");
        return label;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case DIALOG_CLOSE_REQUEST:
                if (dialog.isVisible()){
                    textInput.setText("");
                    dialog.dispose();

                }
                break;
            case DIALOG_ACCEPT_REQUEST:
                if (dialog.isVisible()){
                    acceptFilenameChange();
                }
                break;
        }
    }
}
