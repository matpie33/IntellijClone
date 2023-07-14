package core.ui.components;

import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxColorStyledDocument extends DefaultStyledDocument {

    public static final String KEYWORDS_REGEXP = "(\\W)+(private|public|protected|class|false|true|int|if|else|while|for)";
    private final StyleContext context = StyleContext.getDefaultStyleContext();
    private final AttributeSet keywordColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.RED);
    private final AttributeSet defaultColorAttribute = context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.WHITE);
    private Pattern keywordsPattern = Pattern.compile(KEYWORDS_REGEXP);

    @Override
    public void insertString (int offset, String textToAdd, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(offset, textToAdd, attributeSet);
        if (textToAdd.equals("\n")){
            return;
        }
        if (textToAdd.length()==1){
            String result = findWordEndingAtOffset(offset, textToAdd);
            if ((" "+result).matches(KEYWORDS_REGEXP)){
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
        while (currentIndex>0 && (Character.isLetter(text.charAt(currentIndex)) || text.charAt(currentIndex)=='_')){
            word.append(text.charAt(currentIndex));
            currentIndex--;
        }
        return word.reverse() + str;
    }


    @Override
    public void remove (int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        String word = findWordEndingAtOffset(offset, "");
        Matcher matcher = keywordsPattern.matcher(" "+word);
        if (matcher.matches()){
            setCharacterAttributes(offset -word.length(), word.length(), keywordColorAttribute, false);
        }
        else{
            setCharacterAttributes(offset -word.length(), word.length(), defaultColorAttribute, false);
        }

    }


}
