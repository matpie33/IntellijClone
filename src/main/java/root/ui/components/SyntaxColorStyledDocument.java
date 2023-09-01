package root.ui.components;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import root.core.codecompletion.AvailableClassesFilter;
import root.core.constants.SyntaxModifiers;
import root.core.dto.*;
import root.core.fileio.FileAutoSaver;
import root.core.undoredo.UndoRedoManager;
import root.core.utility.CommentsHandler;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    private final AttributeSet commentColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.GRAY);
    private final AttributeSet defaultColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, new Color(169, 183, 198));
    private Pattern keywordsPattern = Pattern.compile(SyntaxModifiers.KEYWORDS_REGEXP);

    private ApplicationState applicationState;

    private UndoRedoManager undoRedoManager;

    private CodeCompletionPopup codeCompletionPopup;
    private JTextPane textComponent;

    private AvailableClassesFilter availableClassesFilter;

    private StringBuilder wordBeingTyped = new StringBuilder();

    private boolean isTextSettingInProgress;

    private FileAutoSaver fileAutoSaver;

    private CommentsHandler commentsHandler;

    public void setIsTextSettingInProgress(boolean setTextInProgress) {
        this.isTextSettingInProgress = setTextInProgress;
    }

    public SyntaxColorStyledDocument(ApplicationState applicationState, UndoRedoManager undoRedoManager, CodeCompletionPopup codeCompletionPopup, AvailableClassesFilter availableClassesFilter, FileAutoSaver fileAutoSaver, CommentsHandler commentsHandler) {
        this.applicationState = applicationState;
        this.undoRedoManager = undoRedoManager;
        this.codeCompletionPopup = codeCompletionPopup;
        this.availableClassesFilter = availableClassesFilter;
        this.fileAutoSaver = fileAutoSaver;
        this.commentsHandler = commentsHandler;
    }


    public void clearChanges (){
        undoRedoManager.clearChanges();
    }

    public void undo () throws BadLocationException {
        undoRedoManager.addCurrentChangesToList();
        TextChangeDTO undoAction = undoRedoManager.getNextUndoAction();
        if (undoAction instanceof InsertChangeDTO){
            InsertChangeDTO insert = (InsertChangeDTO) undoAction;
            removeInternal(insert.getOffsetWhereChangeStarted(), insert.getChangedText().length());
        }
        if (undoAction instanceof InsertImportChangeDTO){
            applicationState.getClassStructureOfOpenedFile().removeImport(((InsertImportChangeDTO)undoAction).getFullyQualifiedClassName());
        }
        if (undoAction instanceof RemoveChangeDTO){
            RemoveChangeDTO removeAction = (RemoveChangeDTO) undoAction;
            insertInternal(removeAction.getStartingOffset(), removeAction.getRemovedText());
        }
        handleFileChanged();
    }

    public void redo () throws BadLocationException {
        checkForTextChanges();
        TextChangeDTO undoAction = undoRedoManager.getNextRedoAction();
        if (undoAction instanceof InsertChangeDTO){
            InsertChangeDTO insert = (InsertChangeDTO)undoAction;
            insertInternal(insert.getOffsetWhereChangeStarted(), insert.getChangedText());
        }
        if (undoAction instanceof InsertImportChangeDTO){
            applicationState.getClassStructureOfOpenedFile().addImport(((InsertImportChangeDTO)undoAction).getFullyQualifiedClassName());
        }
        if (undoAction instanceof RemoveChangeDTO){
            RemoveChangeDTO removeAction = (RemoveChangeDTO) undoAction;
            removeInternal(removeAction.getStartingOffset(), removeAction.getRemovedText().length());
        }
        handleFileChanged();
    }

    private boolean isAfterLetterOrDigit (int offset) throws BadLocationException {
        if (offset>0){
            String previousCharacter = getText(offset - 1, 1);
            return Character.isLetterOrDigit(previousCharacter.charAt(0));
        }
        return false;
    }

    @Override
    public void insertString (int offset, String textToAdd, AttributeSet attributeSet) throws BadLocationException {
        if (textToAdd.equals("\n") && codeCompletionPopup.isVisible()){
            return;
        }

        if (!isTextSettingInProgress){
            if (textToAdd.length()==1){
                char singleCharacter = textToAdd.charAt(0);
                if (Character.isLetterOrDigit(singleCharacter) && !isInsideComment() && (wordBeingTyped.length() > 0 || (!Character.isDigit(singleCharacter) && !isAfterLetterOrDigit(offset)))) {
                    wordBeingTyped.append(textToAdd);
                    showCodeCompletionPopup();
                }
                else {
                    codeCompletionPopup.hide();
                }
            }

            undoRedoManager.handleInsertChange(textToAdd,offset);
        }

        insertInternal(offset, textToAdd);
        handleFileChanged();


    }

    private void showCodeCompletionPopup() throws BadLocationException {

        Map<String, Collection<ClassNavigationDTO>> suggestedClasses = availableClassesFilter.getClassesStartingWith(wordBeingTyped.toString());
        codeCompletionPopup.clear();
        codeCompletionPopup.addSuggestions(suggestedClasses);
        if (!suggestedClasses.isEmpty()){
            codeCompletionPopup.show(textComponent);
        }
        else{
            codeCompletionPopup.hide();
        }
    }

    private void insertInternal(int offset, String textToAdd) throws BadLocationException {
        super.insertString(offset, textToAdd, defaultColorAttribute);

        if (textToAdd.equals("\n")){
            return;
        }
        if (textToAdd.length()==1){
            WordOffsetDTO wordOffset = findWordEndingAtOffset(offset);
            String word = wordOffset.getWord();
            int startOffset = wordOffset.getStartingOffset();
            if (isInsideComment()){
                setCharacterAttributes(startOffset, word.length(), commentColorAttribute, false);
            }
            else if ((word).matches(SyntaxModifiers.KEYWORDS_REGEXP)){
                setCharacterAttributes(startOffset, word.length(), keywordColorAttribute, false);
            }
            else{
                setCharacterAttributes(startOffset, word.length(), defaultColorAttribute, false);
            }
        }
        else{
            CommentsPositionsDTO commentsPositionsDTO = commentsHandler.findCommentSections(offset, textToAdd);
            commentsPositionsDTO.getCommentedCodeSections().
                    forEach(position->setCharacterAttributes(position.getStartOffset(),
                            position.getLength(), commentColorAttribute, true));
            if (isInsideComment()){
                setCharacterAttributes(offset, textToAdd.length(), commentColorAttribute, true);
            }
            doKeywordsColoring(offset, textToAdd, commentsPositionsDTO);
        }
    }

    private boolean isInsideComment() {
        return commentsHandler.isInMultilineCommentSection();
    }

    private TextPositionDTO offsetToLine0Based(int offset) {
        Element rootElement = getDefaultRootElement();
        for (int i = 0; i < rootElement.getElementCount(); i++) {
            Element element = rootElement.getElement(i);
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            if (endOffset>offset){
                return new TextPositionDTO(i, offset - startOffset-1, startOffset, endOffset);
            }
        }
        throw new IllegalArgumentException("Invalid offset: "+ offset);
    }

    private void colorFieldAccess(ClassStructureDTO classStructure) {
        List<TokenPositionDTO> fieldAccessPositions = classStructure.getFieldAccessPositions();
        for (TokenPositionDTO fieldAccessPosition : fieldAccessPositions) {
            setCharacterAttributes(fieldAccessPosition.getStartOffset(), fieldAccessPosition.getLength(), fieldColorAttribute, false);
        }
        for (FieldDeclarationDTO fieldDeclaration : classStructure.getFieldDeclarations()) {
            setCharacterAttributes(fieldDeclaration.getStartOffset(), fieldDeclaration.getName().length(), fieldColorAttribute, true);
        }
    }

    public void doWordsColoring() {
        ClassStructureDTO classStructure = applicationState.getClassStructureOfOpenedFile();
        if (classStructure==null){
            return;
        }
        colorFieldAccess(classStructure);
    }


    private void doKeywordsColoring(int offsetInDocument, String str, CommentsPositionsDTO commentsPositionsDTO) {
        Matcher matcher = keywordsPattern.matcher(str);
        List<TokenPositionDTO> commentedCodeSections = commentsPositionsDTO.getCommentedCodeSections();
        while (matcher.find()){
            int startingOffset = matcher.start();
            int endOffset = matcher.end();
            startingOffset += offsetInDocument;
            endOffset += offsetInDocument;
            if (isInsideCommentSections(commentedCodeSections, startingOffset)){
                continue;
            }
            setCharacterAttributes(startingOffset, endOffset - startingOffset , keywordColorAttribute, true);
        }

    }

    private boolean isInsideCommentSections(List<TokenPositionDTO> commentedCodeSections, int offset){
        if (commentsHandler.isInMultilineCommentSection()){
            return true;
        }
        for (TokenPositionDTO commentedCodeSection : commentedCodeSections) {
            int startOffset = commentedCodeSection.getStartOffset();
            int endOffset = startOffset + commentedCodeSection.getLength();
            if (startOffset<offset && endOffset> offset){
                return true;
            }
        }
        return false;
    }


    private WordOffsetDTO findWordEndingAtOffset(int offset) throws BadLocationException {
        StringBuilder word = new StringBuilder();
        String text = getText(0, getLength());
        int currentIndex = offset -1;
        while (currentIndex>=0 && isLetterOrUnderscore(text, currentIndex)){
            word.append(text.charAt(currentIndex));
            currentIndex--;
        }
        int startingOffset = currentIndex+1;
        currentIndex = offset;
        word.reverse();
        while (currentIndex < getLength() && isLetterOrUnderscore(text, currentIndex) ){
            word.append(text.charAt(currentIndex));
            currentIndex++;
        }
        return new WordOffsetDTO(word.toString(), startingOffset );
    }

    private boolean isLetterOrUnderscore(String text, int currentIndex) {
        return Character.isLetter(text.charAt(currentIndex)) || text.charAt(currentIndex) == '_';
    }


    @Override
    public void remove (int offset, int length) throws BadLocationException {
        if (wordBeingTyped.length()>0){
            wordBeingTyped.setLength(Math.max(wordBeingTyped.length()-length, 0));
        }
        if (!isTextSettingInProgress){
            showCodeCompletionPopup();
            undoRedoManager.handleRemoveChange(getText(offset, length), offset);
        }

        removeInternal(offset, length);
        handleFileChanged();

    }

    private void handleFileChanged() throws BadLocationException {
        if (!isTextSettingInProgress){
            String text = getText(0, getLength());
            fileAutoSaver.textModified(text);
            applicationState.addCurrentFileToClassesToRecompile();
        }
    }

    private void removeInternal(int offset, int length) throws BadLocationException {

        super.remove(offset, length);
        WordOffsetDTO wordOffsetDTO = findWordEndingAtOffset(offset);
        String word = wordOffsetDTO.getWord();
        Matcher matcher = keywordsPattern.matcher(word);
        if (matcher.matches()){
            setCharacterAttributes(wordOffsetDTO.getStartingOffset(), word.length(), keywordColorAttribute, false);
        }
        else if (!isInsideComment()){
            setCharacterAttributes(wordOffsetDTO.getStartingOffset(), word.length(), defaultColorAttribute, false);
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
        undoRedoManager.addCurrentChangesToList();
    }

    public void clearWordBeingTyped (){
        wordBeingTyped.setLength(0);
    }

    public void setTextComponent(JTextPane textComponent) {
        this.textComponent = textComponent;
    }

    public void selectNextSuggestionOptionally() {
        codeCompletionPopup.selectNextIfVisible();

    }

    public void selectPreviousSuggestionOptionally() {
        codeCompletionPopup.selectPreviousIfVisible();

    }


    public void insertSuggestedText(int offset, ClassSuggestionDTO suggestionSelected) throws BadLocationException {

        undoRedoManager.addCurrentChangesToList();
        int preTypedWordsLength = wordBeingTyped.length();
        remove(offset - preTypedWordsLength, preTypedWordsLength);
        ClassStructureDTO classStructure = applicationState.getClassStructureOfOpenedFile();

        insertString(offset-preTypedWordsLength, suggestionSelected.getClassName(), defaultColorAttribute);
        undoRedoManager.addCurrentChangesToList();

        String fullyQualifiedClassName = String.format("%s.%s", suggestionSelected.getPackageName(), suggestionSelected.getClassName());
        String importText = String.format("import %s;\n", fullyQualifiedClassName);
        boolean classAlreadyHasThisImport = classStructure.containsImport(fullyQualifiedClassName);

        if (!classAlreadyHasThisImport){
            classStructure.addImport(fullyQualifiedClassName);

            TokenPositionDTO packageDeclarationPosition = classStructure.getPackageDeclarationPosition();
            int offsetForImport = 0;
            if (packageDeclarationPosition != null){
                int packageDeclarationOffset = packageDeclarationPosition.getStartOffset();
                offsetForImport = getOffsetForImport(packageDeclarationOffset);
            }

            undoRedoManager.createImportInsertChange(offsetForImport, fullyQualifiedClassName);
            insertString(offsetForImport, importText, defaultColorAttribute);
            undoRedoManager.addCurrentChangesToList();
        }

        wordBeingTyped.setLength(0);
    }

    private int getOffsetForImport(int packageDeclarationOffset) {
        Element rootElement = getDefaultRootElement();
        int offsetForImport  =0 ;
        int lineNumber = 0;
        while (lineNumber < rootElement.getElementCount()) {
            Element element = rootElement.getElement(lineNumber);
            int startOffset = element.getStartOffset();
            if (startOffset > packageDeclarationOffset){
                offsetForImport = startOffset;
                break;
            }
            lineNumber++;
        }
        return offsetForImport;
    }

    public void deleteLine (int caretPosition){
        TextPositionDTO textPositionDTO = offsetToLine0Based(caretPosition);
        int startOffset = textPositionDTO.getStartOffset();
        int endOffset = textPositionDTO.getEndOffset();
        try {
            remove(startOffset, endOffset-startOffset);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public int duplicateLines(int selectionStart, int selectionEnd) {
        try {
            TextPositionDTO startPosition = offsetToLine0Based(selectionStart);
            TextPositionDTO endPosition = offsetToLine0Based(selectionEnd);
            int startOffset = startPosition.getStartOffset();
            int endOffset = endPosition.getEndOffset();
            String text = getText(startOffset, endOffset-startOffset);
            endOffset = Math.min(endOffset, getLength());
            insertString(endOffset, text, defaultColorAttribute);
            return text.length();
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
