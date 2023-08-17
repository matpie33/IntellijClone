package root.core.dto;

public class WordOffsetDTO {

    private String word;

    private int startingOffset;

    public WordOffsetDTO(String word, int startingOffset) {
        this.word = word;
        this.startingOffset = startingOffset;
    }

    public String getWord() {
        return word;
    }

    public int getStartingOffset() {
        return startingOffset;
    }
}
