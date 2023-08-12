package root.ui.components;


import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class ImprovedCaret extends DefaultCaret {

    @Override
    public void mouseClicked(MouseEvent e) {
        int numberOfClicks = e.getClickCount();

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (!e.isConsumed()) {
                adjustCaretAndFocus(e);
                if (numberOfClicks == 2) {
                    try {
                        selectWordInternal(e);
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    private void selectWordInternal(MouseEvent e) throws BadLocationException {
        JTextComponent source = (JTextComponent) e.getSource();
        int caretPosition = source.getCaretPosition();
        Document document = source.getDocument();
        int wordStartIndex = caretPosition;
        int wordEndIndex = caretPosition;
        while (wordStartIndex > 0 && Character.isLetterOrDigit(letterAtOffset(document, wordStartIndex))){
            wordStartIndex--;
        }
        while (wordEndIndex < document.getLength() && Character.isLetterOrDigit(letterAtOffset(document, wordEndIndex))){
            wordEndIndex++;
        }
        source.select(wordStartIndex>0?wordStartIndex+1: wordStartIndex, wordEndIndex);
    }

    private char letterAtOffset(Document document, int offset) throws BadLocationException {
        return document.getText(offset, 1).charAt(0);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        adjustFocus();

    }

    private void adjustCaret(MouseEvent e) {
        if ((e.getModifiersEx() & ActionEvent.SHIFT_MASK) != 0 &&
                getDot() != -1) {
            moveCaret(e);
        } else if (!e.isPopupTrigger()) {
            positionCaret(e);
        }
    }


    private void adjustFocus() {
        if ((getComponent() != null) && getComponent().isEnabled() &&
                getComponent().isRequestFocusEnabled()) {
            getComponent().requestFocus();
        }
    }


    private void adjustCaretAndFocus(MouseEvent e) {
        adjustCaret(e);
        adjustFocus();
    }

}
