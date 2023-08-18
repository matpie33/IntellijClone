package root.core.dto;

public class InsertChangeDTO implements TextChangeDTO {
    private StringBuilder changedTextBuilder = new StringBuilder();
    private int offsetWhereChangeStarted;

    public InsertChangeDTO(int offsetWhereChangeStarted) {
        this.offsetWhereChangeStarted = offsetWhereChangeStarted;
    }

    public void appendText (String text){
        changedTextBuilder.append(text);
    }

    public String getChangedText() {
        return changedTextBuilder.toString();
    }

    public int getOffsetWhereChangeStarted() {
        return offsetWhereChangeStarted;
    }

    @Override
    public String toString() {
        return "InsertChangeDTO{" +
                "singleChangeBuilder=" + changedTextBuilder + " offset : "+offsetWhereChangeStarted+
                '}';
    }
}
