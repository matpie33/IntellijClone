package root.core.undoredo;

import org.springframework.stereotype.Component;
import root.ui.components.SyntaxColorStyledDocument;
import root.ui.uibuilders.TabPaneBuilderUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;

@Component
public class UndoAction extends AbstractAction {

    private TabPaneBuilderUI tabPaneBuilderUI;

    public UndoAction(TabPaneBuilderUI tabPaneBuilderUI) {
        this.tabPaneBuilderUI = tabPaneBuilderUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SyntaxColorStyledDocument document = tabPaneBuilderUI.getDocumentForActiveEditor();
        try {
            document.undo();
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
