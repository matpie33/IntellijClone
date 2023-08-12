package root.core.codecompletion;

import org.springframework.stereotype.Component;
import root.core.dto.ClassSuggestionDTO;
import root.ui.components.CodeCompletionPopup;
import root.ui.components.SyntaxColorStyledDocument;
import root.ui.uibuilders.TabPaneBuilderUI;

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


    private CodeCompletionPopup codeCompletionPopup;

    private TabPaneBuilderUI tabPaneBuilderUI;

    public CodeCompletionNavigator(CodeCompletionPopup codeCompletionPopup, TabPaneBuilderUI tabPaneBuilderUI) {
        this.codeCompletionPopup = codeCompletionPopup;
        this.tabPaneBuilderUI = tabPaneBuilderUI;
        codeCompletionPopup.addMouseListener(this);
        tabPaneBuilderUI.addTabChangeListener(this);
    }

    public void handleCodeCompletionNavigation(KeyEvent e) {
        JTextComponent editorText = (JTextComponent) e.getSource();
        SyntaxColorStyledDocument textDocument = tabPaneBuilderUI.getDocumentForActiveEditor();
        switch (e.getKeyCode()){
            case KeyEvent.VK_DOWN:
                textDocument.selectNextSuggestionOptionally();
                break;
            case KeyEvent.VK_UP:
                textDocument.selectPreviousSuggestionOptionally();
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
        ClassSuggestionDTO suggestionSelected = codeCompletionPopup.getSelectedValue(e);
        int offset = editorText.getCaretPosition();
        SyntaxColorStyledDocument textDocument = tabPaneBuilderUI.getDocumentForActiveEditor();
        try {
            textDocument.insertSuggestedText(offset, suggestionSelected);
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
