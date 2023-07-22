package core.ui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class EditorScrollPane extends JScrollPane {

    private static final long serialVersionUID = 1L;
    private final String fontFamily;
    private final int fontSize;

    private JTextPane editorPane;
    private JTextPane lineNumbersPane;

    public EditorScrollPane(JTextPane editorPane) {
        this.editorPane = editorPane;
        fontFamily = editorPane.getFont().getFamily();
        fontSize = editorPane.getFont().getSize();
        Document document = editorPane.getDocument();

        lineNumbersPane = new JTextPane();
        lineNumbersPane.setBackground(new Color(92, 103, 129));
        lineNumbersPane.setEditable(false);

        SimpleAttributeSet alignmentAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(alignmentAttribute, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setRightIndent(alignmentAttribute, 5);
        lineNumbersPane.setParagraphAttributes(alignmentAttribute, true);
        lineNumbersPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(100, 112, 140)));

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                adjustLineNumbers();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                adjustLineNumbers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                adjustLineNumbers();
            }
        });
        this.setFont(editorPane.getFont());
        this.getViewport().add(editorPane);

        this.setRowHeaderView(lineNumbersPane);
    }


    private void adjustLineNumbers() {
        try {
            String editorText = editorPane.getText();

            SimpleAttributeSet plain = new SimpleAttributeSet();
            StyleConstants.setFontFamily(plain, fontFamily);
            StyleConstants.setFontSize(plain, fontSize);
            StyleConstants.setForeground(plain, new Color(155, 164, 183));

            Document lineNumbersDocument = lineNumbersPane.getDocument();
            lineNumbersDocument.remove(0, lineNumbersDocument.getLength());

            int length = editorText.length() - editorText.replaceAll("\n", "").length() + 1;

            for (int i = 1; i <= length; i++) {
                lineNumbersDocument.insertString(lineNumbersDocument.getLength(), i + "\n", plain);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}