package root.core.dto;

import com.github.javaparser.Position;

public class ClassStructureTreeElementDTO {

    private String displayName;
    private Position startingPosition;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Position startingPosition) {
        this.startingPosition = startingPosition;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
