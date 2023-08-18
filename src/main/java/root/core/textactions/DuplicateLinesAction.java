package root.core.textactions;

import org.springframework.stereotype.Component;
import root.ui.components.SyntaxColorStyledDocument;
import root.ui.uibuilders.TabPaneBuilderUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class DuplicateLinesAction extends AbstractAction {

    private TabPaneBuilderUI tabPaneBuilderUI;

    public DuplicateLinesAction(TabPaneBuilderUI tabPaneBuilderUI) {
        this.tabPaneBuilderUI = tabPaneBuilderUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextPane textComponent = tabPaneBuilderUI.getTextComponentFromActiveTab();
        int selectionStart = textComponent.getSelectionStart();
        int selectionEnd = textComponent.getSelectionEnd();
        SyntaxColorStyledDocument document = tabPaneBuilderUI.getDocumentForActiveEditor();
        int duplicateLineSize = document.duplicateLines(selectionStart, selectionEnd);
        selectionStart+=duplicateLineSize;
        selectionEnd+=duplicateLineSize;
        textComponent.setSelectionStart(selectionStart);
        textComponent.setSelectionEnd(selectionEnd);
    }
}
