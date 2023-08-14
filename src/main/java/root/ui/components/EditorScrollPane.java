package root.ui.components;

import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditorScrollPane extends JScrollPane {

    private static final long serialVersionUID = 1L;
    public static final String NEW_LINE = "\n";
    public static final String UNIT_SCROLL_UP = "unitScrollUp";
    public static final String UNIT_SCROLL_DOWN = "unitScrollDown";
    private final String fontFamily;
    private final int fontSize;
    private final SimpleAttributeSet fontAttributeSet;
    private final JTextPane textEditor;

    private JTextPane lineNumbersPane;
    private int currentLinesCount = 0;

    public EditorScrollPane(JTextPane textEditor) {
        clearAction(UNIT_SCROLL_UP);
        clearAction(UNIT_SCROLL_DOWN);
        this.textEditor = textEditor;
        fontFamily = textEditor.getFont().getFamily();
        fontSize = textEditor.getFont().getSize();

        lineNumbersPane = new JTextPane();
        lineNumbersPane.setBackground(new Color(92, 103, 129));
        lineNumbersPane.setEditable(false);
        fontAttributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(fontAttributeSet, fontSize);
        StyleConstants.setFontFamily(fontAttributeSet, fontFamily);
        lineNumbersPane.setCharacterAttributes(fontAttributeSet, true);

        textEditor.getDocument().addDocumentListener(new DocumentListener() {
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

        this.setFont(textEditor.getFont());
        this.getViewport().add(textEditor);

        this.setRowHeaderView(lineNumbersPane);
    }

    public void setUpdateCaret (boolean update){
        DefaultCaret caret1 = (DefaultCaret) textEditor.getCaret();
        DefaultCaret caret2 = (DefaultCaret) lineNumbersPane.getCaret();
        if (update){
            caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            caret1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
        else{
            caret2.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            caret1.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        }
    }

    private void clearAction(String actionName) {
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    public JTextPane getTextEditor() {
        return textEditor;
    }
}