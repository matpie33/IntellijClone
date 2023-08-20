package root.ui.components;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.constants.ClassType;
import root.core.dto.CreateClassDTO;
import root.core.keylisteners.CreateJavaClassKeyListener;

import javax.swing.*;
import java.awt.*;

@Component
public class CreateClassPopup extends JPopupMenu {

    public static final int POPUP_WIDTH = 200;
    public static final int POPUP_HEIGHT = 400;
    private final JTextField inputField;
    private final JList<String> list;


    public CreateClassPopup (CreateJavaClassKeyListener createJavaClassKeyListener){
        list = createList();
        list.setSelectedIndex(0);

        createJavaClassKeyListener.setList(list);
        JPanel popupPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(popupPanel, BoxLayout.Y_AXIS);
        popupPanel.setLayout(boxLayout);

        JLabel label = new JLabel("Create class");

        inputField = new JTextField(20);
        inputField.addKeyListener(createJavaClassKeyListener);

        JPanel listFillerPanel = new JPanel(new BorderLayout());
        listFillerPanel.add(list);

        popupPanel.add(label);
        popupPanel.add(inputField);
        popupPanel.add(listFillerPanel);
        add(popupPanel);
        setVisible(false);
    }

    public CreateClassDTO getData (){
        return new CreateClassDTO(inputField.getText(), ClassType.fromUiValue(list.getSelectedValue()));
    }

    public void doShow(){
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - POPUP_WIDTH) / 2);
        int y = (int) ((dimension.getHeight() - POPUP_HEIGHT) / 2);
        show(Main.FRAME, x, y);
        inputField.requestFocus();

    }

    private JList<String> createList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        model.addElement("Class");
        model.addElement("Interface");
        model.addElement("Enum");
        model.addElement("Annotation");
        return list;
    }

    public void clear() {
        list.setSelectedIndex(0);
        inputField.setText("");
        setVisible(false);
    }
}
