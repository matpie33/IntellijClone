package root.core.undoredo;

import org.springframework.stereotype.Component;
import root.ui.uibuilders.TabPaneBuilderUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;

@Component
public class RedoAction extends AbstractAction {
    private TabPaneBuilderUI tabPaneBuilderUI;

    public RedoAction(TabPaneBuilderUI tabPaneBuilderUI) {
        this.tabPaneBuilderUI = tabPaneBuilderUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            tabPaneBuilderUI.getDocumentForActiveEditor().redo();
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
