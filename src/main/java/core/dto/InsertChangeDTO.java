package core.dto;

import javax.swing.text.AttributeSet;

public class InsertChangeDTO implements TextChangeDTO {
    private StringBuilder singleChangeBuilder = new StringBuilder();
    private int offsetWhereChangeStarted;
    private AttributeSet attributesForChangedText;

    public InsertChangeDTO(int offsetWhereChangeStarted, AttributeSet attributesForChangedText) {
        this.offsetWhereChangeStarted = offsetWhereChangeStarted;
        this.attributesForChangedText = attributesForChangedText;
    }

    public void appendText (String text){
        singleChangeBuilder.append(text);
    }

    public StringBuilder getSingleChangeBuilder() {
        return singleChangeBuilder;
    }

    public int getOffsetWhereChangeStarted() {
        return offsetWhereChangeStarted;
    }

    public AttributeSet getAttributesForChangedText() {
        return attributesForChangedText;
    }

    @Override
    public String toString() {
        return "InsertChangeDTO{" +
                "singleChangeBuilder=" + singleChangeBuilder +
                '}';
    }
}
