package core.keylisteners;

import core.ui.components.CodeCompletionPopup;
import core.ui.components.SyntaxColorStyledDocument;
import org.springframework.stereotype.Component;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;

@Component
public class CodeCompletionNavigator {


    private final SyntaxColorStyledDocument textDocument;
    private CodeCompletionPopup codeCompletionPopup;

    public CodeCompletionNavigator(CodeCompletionPopup codeCompletionPopup, SyntaxColorStyledDocument textDocument) {
        this.codeCompletionPopup = codeCompletionPopup;
        this.textDocument = textDocument;
    }

    public void handleCodeCompletionNavigation(KeyEvent e) {
        JTextComponent editorText = (JTextComponent) e.getSource();
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            codeCompletionPopup.requestFocus(e);
            textDocument.selectNextSuggestionOptionally();
            editorText.requestFocusInWindow();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            codeCompletionPopup.requestFocus(e);
            textDocument.selectPreviousSuggestionOptionally();
            editorText.requestFocusInWindow();
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            if (!codeCompletionPopup.isVisible()){
                return;
            }
            String selectedValue = codeCompletionPopup.getSelectedValue(e);
            int offset = editorText.getCaretPosition();
            try {
                textDocument.insertText(offset, selectedValue);
                codeCompletionPopup.hide();
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            codeCompletionPopup.hide();
        }
    }

}
