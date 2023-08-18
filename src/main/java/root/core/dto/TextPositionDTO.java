package root.core.dto;

public class TextPositionDTO {

    private int lineNumber;

    private int columnNumber;

    private int startOffset;

    private int endOffset;

    public TextPositionDTO(int lineNumber, int columnNumber, int startOffset, int endOffset) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }
}
