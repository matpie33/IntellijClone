package root.core.dto;

public class ClassDeclarationDTO {

    private int startingOffset;

    private String name;

    public ClassDeclarationDTO(int startingOffset, String name) {
        this.startingOffset = startingOffset;
        this.name = name;
    }

    public int getStartingOffset() {
        return startingOffset;
    }

    public String getName() {
        return name;
    }
}
