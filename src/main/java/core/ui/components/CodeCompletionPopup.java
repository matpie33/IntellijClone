package core.ui.components;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Set;

@Component
public class CodeCompletionPopup extends MouseAdapter implements WindowFocusListener {

    public static final int POPUP_OFFSET_Y_FROM_CARET = 20;
    public static final String CARET_DOWN = "caret-down";
    public static final String CARET_UP = "caret-up";
    private final JList<String> list;
    private final DefaultListModel<String> listModel;
    private JPopupMenu popup;
    private ActionMap currentTextFieldActionMap;

    public CodeCompletionPopup(){
        popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(700, 300));
        JPanel contentPanel = new JPanel();

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(list);
        contentPanel.add(scrollPane);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        popup.add(contentPanel);

    }

    public void hide (){
        popup.setVisible(false);
        clear();
        if (currentTextFieldActionMap !=null){
            currentTextFieldActionMap.get(CARET_DOWN).setEnabled(true);
            currentTextFieldActionMap.get(CARET_UP).setEnabled(true);

        }

    }

    public void addSuggestions(Set<String> text){
        text.forEach(listModel::addElement);
        if (listModel.getSize()==1){
            list.setSelectedIndex(0);
        }
    }

    public void show(JTextComponent textComponent) throws BadLocationException {
        if (!textComponent.isShowing() || popup.isVisible()){
            return;
        }
        currentTextFieldActionMap = textComponent.getActionMap();

        currentTextFieldActionMap.get(CARET_DOWN).setEnabled(false);
        currentTextFieldActionMap.get(CARET_UP).setEnabled(false);
        Rectangle2D caretPositionRelativeToTextPane = textComponent.modelToView2D(textComponent.getCaretPosition());
        Point textPaneLocation = textComponent.getLocationOnScreen();
        popup.setVisible(true);
        popup.setLocation(new Point(textPaneLocation.x +(int)caretPositionRelativeToTextPane.getX(), textPaneLocation.y + (int)caretPositionRelativeToTextPane.getY()+ POPUP_OFFSET_Y_FROM_CARET));
    }

    public void selectNextIfVisible() {
        if (popup.isVisible()){
            if (list.getSelectedIndex() < listModel.getSize()-1){
                list.setSelectedIndex(list.getSelectedIndex()+1);
            }
        }
    }

    public void clear(){
        listModel.removeAllElements();
    }

    public  boolean isVisible (){
        return popup.isVisible();
    }

    public String getSelectedValue(InputEvent e) {

        if (popup.isVisible()){
            e.consume();
        }
        return list.getSelectedValue();
    }

    public void selectPreviousIfVisible() {
        if (popup.isVisible()){
            if (list.getSelectedIndex() >0){
                list.setSelectedIndex(list.getSelectedIndex()-1);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        hide();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        hide();;
    }

    public void addMouseListener(MouseListener mouseListener) {
        list.addMouseListener(mouseListener);
    }
}
