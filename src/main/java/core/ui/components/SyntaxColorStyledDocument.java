package core.ui.components;

import com.github.javaparser.Range;
import core.backend.UndoRedoManager;
import core.constants.SyntaxModifiers;
import core.dto.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class SyntaxColorStyledDocument extends DefaultStyledDocument  {

    public static final int SUPPORTED_TABS_AMOUNT = 200;
    public static final int TAB_SIZE = 4;
    private final StyleContext context = StyleContext.getDefaultStyleContext();
    private final AttributeSet keywordColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, new Color(161, 74, 44));
    private final AttributeSet fieldColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, new Color(101, 63, 226));
    private final AttributeSet defaultColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, new Color(169, 183, 198));
    private Pattern keywordsPattern = Pattern.compile(SyntaxModifiers.KEYWORDS_REGEXP);

    private ApplicatonState applicatonState;

    private UndoRedoManager undoRedoManager;

    private InsertChangeDTO insertChangeDTO;

    private RemoveChangeDTO removeChangeDTO;


    public SyntaxColorStyledDocument(ApplicatonState applicatonState, UndoRedoManager undoRedoManager) {
        this.applicatonState = applicatonState;
        this.undoRedoManager = undoRedoManager;
    }

    public void clearChanges (){
        insertChangeDTO = null;
        removeChangeDTO = null;
    }

    public void undo () throws BadLocationException {
        checkForTextChanges();
        TextChangeDTO undoAction = undoRedoManager.getNextUndoAction();
        if (undoAction instanceof InsertChangeDTO){
            InsertChangeDTO insert = (InsertChangeDTO) undoAction;
            super.remove(insert.getOffsetWhereChangeStarted(), insert.getSingleChangeBuilder().length());
        }
        if (undoAction instanceof RemoveChangeDTO){
            RemoveChangeDTO removeAction = (RemoveChangeDTO) undoAction;
            super.insertString(removeAction.getStartingOffset(), removeAction.getTextRemoved().toString(),defaultColorAttribute);
        }
    }

    public void redo () throws BadLocationException {
        checkForTextChanges();
        TextChangeDTO undoAction = undoRedoManager.getNextRedoAction();
        if (undoAction instanceof InsertChangeDTO){
            InsertChangeDTO insert = (InsertChangeDTO)undoAction;
            super.insertString(insert.getOffsetWhereChangeStarted(), insert.getSingleChangeBuilder().toString(), insert.getAttributesForChangedText());
        }
        if (undoAction instanceof RemoveChangeDTO){
            RemoveChangeDTO removeAction = (RemoveChangeDTO) undoAction;
            super.remove(removeAction.getStartingOffset(), removeAction.getTextRemoved().length());
        }
    }

    @Override
    public void insertString (int offset, String textToAdd, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(offset, textToAdd, defaultColorAttribute);
        if (insertChangeDTO == null){
            insertChangeDTO = new InsertChangeDTO(offset, attributeSet);
        }
        insertChangeDTO.appendText(textToAdd);
        Element rootElement = getDefaultRootElement();
        int lineNumber = rootElement.getElementIndex(offset);
        ClassStructureDTO classStructure = applicatonState.getClassStructureOfOpenedFile();
        if (classStructure == null){
            return;
        }
        List<Range> fieldAccessPositions = classStructure.getFieldAccessPositionsAtLine(lineNumber);
        if (!fieldAccessPositions.isEmpty()){
            for (Range fieldAccessPosition : fieldAccessPositions) {
                int startOffset = rootElement.getElement(lineNumber).getStartOffset() + fieldAccessPosition.begin.column;
                int length = fieldAccessPosition.end.column - fieldAccessPosition.begin.column + 1;
                setCharacterAttributes(startOffset,length, fieldColorAttribute, false);

            }
        }

        if (textToAdd.equals("\n")){
            return;
        }
        if (textToAdd.length()==1){
            String result = findWordEndingAtOffset(offset, textToAdd).stripTrailing();
            if ((result).matches(SyntaxModifiers.KEYWORDS_REGEXP)){
                setCharacterAttributes(offset+1-result.length(),result.length(), keywordColorAttribute, false);
            }
            else{
                setCharacterAttributes(offset+1-result.length(),result.length(), defaultColorAttribute, false);
            }
        }
        else{
            colorWord(offset, textToAdd);
        }

    }

    private void colorWord(int offset, String str) {
        Matcher matcher = keywordsPattern.matcher(str);
        boolean anyMatch = false;
        while (matcher.find()){
            int start = matcher.start();
            int end = matcher.end();
            String word = matcher.group();
            int indexOfFirstLetter = 0;
            while (!Character.isLetter(word.charAt(indexOfFirstLetter))){
                indexOfFirstLetter++;
            }
            start += indexOfFirstLetter + offset;
            end += offset;
            setCharacterAttributes(start, end - start , keywordColorAttribute, false);
            anyMatch = true;
        }
        if (!anyMatch){
            setCharacterAttributes(offset, str.length(), defaultColorAttribute, false);
        }
    }


    private String findWordEndingAtOffset(int offset, String str) throws BadLocationException {
        StringBuilder word = new StringBuilder();
        String text = getText(0, offset);
        int currentIndex = offset -1;
        while (currentIndex>=0 && (Character.isLetter(text.charAt(currentIndex)) || text.charAt(currentIndex)=='_')){
            word.append(text.charAt(currentIndex));
            currentIndex--;
        }
        return word.reverse() + str;
    }


    @Override
    public void remove (int offset, int length) throws BadLocationException {
        String removedText = getText(offset, length);
        super.remove(offset, length);
        if (insertChangeDTO != null){
            undoRedoManager.addNewChange(insertChangeDTO);
            insertChangeDTO = null;
        }
        if (removeChangeDTO == null){
            removeChangeDTO = new RemoveChangeDTO(offset);
        }
        removeChangeDTO.appendText(removedText);
        removeChangeDTO.setStartingOffset(offset);
        String word = findWordEndingAtOffset(offset, "");
        Matcher matcher = keywordsPattern.matcher(word);
        if (matcher.matches()){
            setCharacterAttributes(offset -word.length(), word.length(), keywordColorAttribute, false);
        }
        else{
            setCharacterAttributes(offset -word.length(), word.length(), defaultColorAttribute, false);
        }

    }


    public void initialize(Font font, JTextPane editorText) {
        TabStop[] tabs = new TabStop[SUPPORTED_TABS_AMOUNT];
        FontMetrics fontMetrics = editorText.getFontMetrics(font);

        int width = fontMetrics.charWidth(' ');
        for (int i=0; i<SUPPORTED_TABS_AMOUNT; i++){
            tabs[i] = new TabStop(width *(TAB_SIZE *(i+1)), TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
        }
        TabSet tabSet = new TabSet(tabs);
        AttributeSet attributeSet = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        setParagraphAttributes(0, 0, attributeSet, false);
    }


    public void checkForTextChanges() {
        if (insertChangeDTO != null){
            undoRedoManager.addNewChange(insertChangeDTO);
            insertChangeDTO = null;
        }
        if (removeChangeDTO != null){
            undoRedoManager.addNewChange(removeChangeDTO);
            removeChangeDTO = null;
        }
    }
}
