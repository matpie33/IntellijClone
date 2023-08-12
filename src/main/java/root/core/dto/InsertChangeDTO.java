package root.core.dto;

public class InsertChangeDTO implements TextChangeDTO {
    private StringBuilder singleChangeBuilder = new StringBuilder();
    private int offsetWhereChangeStarted;

    public InsertChangeDTO(int offsetWhereChangeStarted) {
        this.offsetWhereChangeStarted = offsetWhereChangeStarted;
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

    @Override
    public String toString() {
        return "InsertChangeDTO{" +
                "singleChangeBuilder=" + singleChangeBuilder +
                '}';
    }
}
