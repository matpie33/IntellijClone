package root.core.textactions;

import org.springframework.stereotype.Component;
import root.ui.components.SyntaxColorStyledDocument;
import root.ui.uibuilders.TabPaneBuilderUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class DeleteLineAction extends AbstractAction {

    private TabPaneBuilderUI tabPaneBuilderUI;

    public DeleteLineAction(TabPaneBuilderUI tabPaneBuilderUI) {
        this.tabPaneBuilderUI = tabPaneBuilderUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextPane textComponent = tabPaneBuilderUI.getTextComponentFromActiveTab();
        int caretPosition = textComponent.getCaretPosition();
        SyntaxColorStyledDocument document = tabPaneBuilderUI.getDocumentForActiveEditor();
        document.deleteLine(caretPosition);
    }
}
