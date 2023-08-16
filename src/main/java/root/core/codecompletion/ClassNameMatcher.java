package root.core.codecompletion;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClassNameMatcher {

    public ClassMatchType doesClassNameMatch (String className, String inputText){
        List<String> words = splitToWords(className);

        int numberOfMatchingFirstLettersOfWords = getNumberOfMatchingFirstLettersOfWords(words, inputText);
        if (inputText.equalsIgnoreCase(className)){
            return ClassMatchType.FULL_MATCH;
        }
        else if (numberOfMatchingFirstLettersOfWords==words.size() && numberOfMatchingFirstLettersOfWords==inputText.length()){
            return ClassMatchType.PARTIAL_MATCH;
        }
        else if (numberOfMatchingFirstLettersOfWords>1){
            return ClassMatchType.NO_MATCH;
        }
        else{
            return isMatchingConsecutiveLetters(inputText, words);
        }

    }

    private ClassMatchType isMatchingConsecutiveLetters(String inputText, List<String> words) {
        int inputTextIndex = 0;
        for (String word : words) {
            int wordLetterIndex = word.indexOf(inputText.charAt(inputTextIndex));
            if (wordLetterIndex != -1) {
                do {
                    wordLetterIndex++;
                    inputTextIndex++;
                    if (inputTextIndex == inputText.length()) {
                        return ClassMatchType.PARTIAL_MATCH;
                    }
                }
                while (wordLetterIndex < word.length() &&
                        word.charAt(wordLetterIndex) == inputText.charAt(inputTextIndex));

            }
        }
        return ClassMatchType.NO_MATCH;
    }

    private int getNumberOfMatchingFirstLettersOfWords(List<String> words, String inputText) {
        int inputTextIndex = 0;
        for (String word : words) {
            if (word.charAt(0) == inputText.charAt(inputTextIndex)) {
                inputTextIndex++;
            }
            if (inputTextIndex == inputText.length()) {
                break;
            }
        }
        return inputTextIndex;
    }

    private List<String> splitToWords(String className) {
        List<String> words = new ArrayList<>();
        StringBuilder wordBuilder = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char character = className.charAt(i);
            if (Character.isUpperCase(character)){
                if (wordBuilder.length()>0){
                    words.add(wordBuilder.toString());
                }
                wordBuilder.setLength(0);
            }
            wordBuilder.append(character);
        }
        if (wordBuilder.length()>0){
            words.add(wordBuilder.toString());
        }
        return words;
    }

}
