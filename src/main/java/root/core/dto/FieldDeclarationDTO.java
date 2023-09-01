package root.core.dto;

public class FieldDeclarationDTO {

    private int startOffset;

    private String name;

    private String modifiers;

    private String type;

    public FieldDeclarationDTO(int startOffset, String name, String modifiers, String type) {
        this.startOffset = startOffset;
        this.name = name;
        this.modifiers = modifiers;
        this.type = type;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public String getName() {
        return name;
    }

    public String getModifiers() {
        return modifiers;
    }

    public String getType() {
        return type;
    }
}
