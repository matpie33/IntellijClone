package root.core.dto;

public class RemoveChangeDTO implements TextChangeDTO{

    private int startingOffset;
    private StringBuilder textRemoved = new StringBuilder();

    public RemoveChangeDTO(int startingOffset) {
        this.startingOffset = startingOffset;
    }

    public StringBuilder getTextRemoved() {
        return textRemoved;
    }

    public int getStartingOffset() {
        return startingOffset;
    }

    public void setStartingOffset(int startingOffset) {
        this.startingOffset = startingOffset;
    }

    public void appendText (String text){

        textRemoved.insert(0, text);
        System.out.println(textRemoved);
    }

    @Override
    public String toString() {
        return "RemoveChangeDTO{" +
                "textRemoved=" + textRemoved +
                '}';
    }
}
