package core.ui.components;

import core.dto.ClassSugestionDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Map;

@Component
public class CodeCompletionPopup extends MouseAdapter implements WindowFocusListener {

    public static final int POPUP_OFFSET_Y_FROM_CARET = 20;
    public static final String CARET_DOWN = "caret-down";
    public static final String CARET_UP = "caret-up";
    private final JList<ClassSugestionDTO> list;
    private final DefaultListModel<ClassSugestionDTO> listModel;
    private final JScrollPane scrollPane;
    private JPopupMenu popup;
    private ActionMap currentTextFieldActionMap;

    public CodeCompletionPopup(){
        popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(700, 300));
        JPanel contentPanel = new JPanel();

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        scrollPane = new JScrollPane(list);
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

    public void addSuggestions(Map<String, Collection<String>> suggestions){
        if (suggestions.isEmpty()){
            hide();
        }
        for (Map.Entry<String, Collection<String>> entry : suggestions.entrySet()) {
            String className = entry.getKey();
            Collection<String> packageNames = entry.getValue();
            for (String packageName : packageNames) {
                ClassSugestionDTO classSugestionDTO = new ClassSugestionDTO(className, packageName);
                listModel.addElement(classSugestionDTO);
            }
        }
        if (!listModel.isEmpty()){
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
                int index = list.getSelectedIndex() + 1;
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    }

    public void clear(){
        listModel.removeAllElements();
    }

    public  boolean isVisible (){
        return popup.isVisible();
    }

    public ClassSugestionDTO getSelectedValue(InputEvent e) {

        if (popup.isVisible()){
            e.consume();
        }
        return list.getSelectedValue();
    }

    public void selectPreviousIfVisible() {
        if (popup.isVisible()){
            if (list.getSelectedIndex() >0){
                int index = list.getSelectedIndex() - 1;
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
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
