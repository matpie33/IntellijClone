package root.core.dto;

public class RemoveChangeDTO implements TextChangeDTO{

    private int startingOffset;
    private StringBuilder removedTextBuilder = new StringBuilder();

    public RemoveChangeDTO(int startingOffset) {
        this.startingOffset = startingOffset;
    }

    public String getRemovedText() {
        return removedTextBuilder.toString();
    }

    public int getStartingOffset() {
        return startingOffset;
    }

    public void appendText (String text){
        removedTextBuilder.insert(0, text);
    }

    @Override
    public String toString() {
        return "RemoveChangeDTO{" +
                "textRemoved=" + removedTextBuilder + " offset "+startingOffset+
                '}';
    }
}
