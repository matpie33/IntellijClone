package core.keylisteners;

import core.ui.components.CodeCompletionPopup;
import core.ui.components.SyntaxColorStyledDocument;
import core.uibuilders.TabPaneBuilderUI;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class CodeCompletionNavigator extends MouseAdapter implements ChangeListener {


    private final SyntaxColorStyledDocument textDocument;
    private CodeCompletionPopup codeCompletionPopup;

    private TabPaneBuilderUI tabPaneBuilderUI;

    public CodeCompletionNavigator(CodeCompletionPopup codeCompletionPopup, SyntaxColorStyledDocument textDocument, TabPaneBuilderUI tabPaneBuilderUI) {
        this.codeCompletionPopup = codeCompletionPopup;
        this.tabPaneBuilderUI = tabPaneBuilderUI;
        codeCompletionPopup.addMouseListener(this);
        tabPaneBuilderUI.addTabChangeListener(this);
        this.textDocument = textDocument;
    }

    public void handleCodeCompletionNavigation(KeyEvent e) {
        JTextComponent editorText = (JTextComponent) e.getSource();
        switch (e.getKeyCode()){
            case KeyEvent.VK_DOWN:
                codeCompletionPopup.requestFocus(e);
                textDocument.selectNextSuggestionOptionally();
                editorText.requestFocusInWindow();
                break;
            case KeyEvent.VK_UP:
                codeCompletionPopup.requestFocus(e);
                textDocument.selectPreviousSuggestionOptionally();
                editorText.requestFocusInWindow();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_SPACE:
                codeCompletionPopup.hide();
                textDocument.clearWordBeingTyped();
                break;
            case KeyEvent.VK_ENTER:
                if (!codeCompletionPopup.isVisible()){
                    return;
                }
                insertSelectedValue(e, editorText);
                break;
            case KeyEvent.VK_ESCAPE:
                codeCompletionPopup.hide();
                break;


        }
    }

    public void insertSelectedValue(InputEvent e, JTextComponent editorText) {
        String selectedValue = codeCompletionPopup.getSelectedValue(e);
        int offset = editorText.getCaretPosition();
        try {
            textDocument.insertSuggestedText(offset, selectedValue);
            codeCompletionPopup.hide();
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount()==2){
            JTextPane editorText = tabPaneBuilderUI.getTextComponentFromActiveTab();
            insertSelectedValue(e, editorText);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        codeCompletionPopup.hide();
    }
}
