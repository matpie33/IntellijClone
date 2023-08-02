package core.dto;

public class TreeNodeFileDTO {

    public enum Type {
        CLASS_FROM_JAR, SOURCE_CLASS, DIRECTORY, EMPTY
    }

    private Type type;

    private String displayName;

    private String path;

    public String getPath() {
        return path;
    }

    public Type getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TreeNodeFileDTO(Type type, String displayName, String path) {
        this.type = type;
        this.displayName = displayName;
        this.path = path;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
