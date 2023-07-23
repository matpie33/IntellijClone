package core.ui.components;

import org.springframework.util.StringUtils;

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
    public static final String NEW_LINE = "\n";
    private final String fontFamily;
    private final int fontSize;
    private final SimpleAttributeSet fontAttributeSet;

    private JTextPane lineNumbersPane;
    private int currentLinesCount = 0;

    public EditorScrollPane(JTextPane editorPane) {
        fontFamily = editorPane.getFont().getFamily();
        fontSize = editorPane.getFont().getSize();

        lineNumbersPane = new JTextPane();
        lineNumbersPane.setBackground(new Color(92, 103, 129));
        lineNumbersPane.setEditable(false);
        fontAttributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(fontAttributeSet, fontSize);
        StyleConstants.setFontFamily(fontAttributeSet, fontFamily);
        lineNumbersPane.setCharacterAttributes(fontAttributeSet, true);

        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                    try {
                        String addedText = e.getDocument().getText(e.getOffset(), e.getLength());
                        int amountOfNewLines = StringUtils.countOccurrencesOf(addedText, NEW_LINE);
                        int previousLinesCount = currentLinesCount;
                        currentLinesCount += amountOfNewLines;
                        if (previousLinesCount ==0 && amountOfNewLines==0){
                            currentLinesCount =1;
                        }
                        Document document = lineNumbersPane.getDocument();
                        for (int i = previousLinesCount+1; i<= currentLinesCount; i++){
                            document.insertString(document.getLength(), i + NEW_LINE, fontAttributeSet);

                        }
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int updatedLinesCount = e.getDocument().getDefaultRootElement().getElementCount();
                if (updatedLinesCount< currentLinesCount){
                    try {
                        Document document = lineNumbersPane.getDocument();
                        int offsetToLineNumberToBeDeleted =document.getDefaultRootElement().getElement(updatedLinesCount).getStartOffset();
                        int amountOfCharactersToDelete = document.getLength() - offsetToLineNumberToBeDeleted;
                        document.remove(offsetToLineNumberToBeDeleted, amountOfCharactersToDelete);
                        currentLinesCount = updatedLinesCount;
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        SimpleAttributeSet alignmentAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(alignmentAttribute, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setRightIndent(alignmentAttribute, 5);
        lineNumbersPane.setParagraphAttributes(alignmentAttribute, true);
        //border top has to be 2, otherwise the line numbers are not synchronized with text correctly
        lineNumbersPane.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 3, new Color(100, 112, 140)));

        this.setFont(editorPane.getFont());
        this.getViewport().add(editorPane);

        this.setRowHeaderView(lineNumbersPane);
    }



}