package root.ui.components;

import javax.swing.*;
import javax.swing.text.StyledDocument;

public class FileEditorComponent extends JTextPane {

    private final SyntaxColorStyledDocument syntaxColorStyledDocument;

    public FileEditorComponent (SyntaxColorStyledDocument syntaxColorStyledDocument){
        super(syntaxColorStyledDocument);
        this.syntaxColorStyledDocument = syntaxColorStyledDocument;
    }

    @Override
    public void setStyledDocument(StyledDocument doc) {
        super.setStyledDocument(doc);
        SyntaxColorStyledDocument s = (SyntaxColorStyledDocument) doc;
        s.setTextComponent(this);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width
                <= getParent().getSize().width;
    }

    @Override
    public void setText(String t) {
        syntaxColorStyledDocument.setIsTextSettingInProgress(true);
        super.setText(t);
        syntaxColorStyledDocument.setIsTextSettingInProgress(false);
        syntaxColorStyledDocument.clearChanges();
        syntaxColorStyledDocument.doWordsColoring();
    }

}
