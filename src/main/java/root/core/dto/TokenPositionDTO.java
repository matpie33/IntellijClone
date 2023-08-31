package root.core.dto;

public class TokenPositionDTO {

    private int startOffset;

    private int length;

    public TokenPositionDTO(int startOffset, int length) {
        this.startOffset = startOffset;
        this.length = length;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getLength() {
        return length;
    }
}
