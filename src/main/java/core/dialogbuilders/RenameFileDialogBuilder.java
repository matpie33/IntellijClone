package core.dialogbuilders;

import core.dto.RenamedFileDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.File;

@Component
public class RenameFileDialogBuilder {

    private JDialog dialog;

    private UIEventsQueue uiEventsQueue;
    private File file;
    private JTextField textInput;

    public RenameFileDialogBuilder(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }

    @PostConstruct
    public void init (){
        JPanel panel = initPanel();
        addComponents(panel);
        initDialog(panel);

    }

    public void showDialog(Point position, File file) {
        this.file = file;
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
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        rootPane.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textInput.setText("");
                dialog.dispose();
            }
        });
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
        RenamedFileDTO renamedFileDTO = new RenamedFileDTO(file, textInput.getText());
        uiEventsQueue.dispatchEvent(UIEventType.FILENAME_CHANGED, renamedFileDTO);
        textInput.setText("");
    }

    private JTextField createTextField() {
        textInput = new JTextField(20);
        textInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "accept");
        textInput.getActionMap().put("accept", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acceptFilenameChange();
            }
        });
        return textInput;
    }

    private JLabel createHeader() {
        JLabel label = new JLabel();
        label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        label.setText("Rename file");
        return label;
    }

}
