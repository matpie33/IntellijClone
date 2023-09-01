package root.core.dto;


public class ClassStructureTreeElementDTO {

    private String displayName;
    private int startingPosition;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(int startingPosition) {
        this.startingPosition = startingPosition;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
