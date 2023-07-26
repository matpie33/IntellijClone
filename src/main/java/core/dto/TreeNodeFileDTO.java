package core.dto;

public class TreeNodeFileDTO {

    public enum Type {
        CLASS_FROM_JAR, SOURCE_CLASS, DIRECTORY, EMPTY
    }

    private Type type;

    private String jarPath;

    private String displayName;

    public Type getType() {
        return type;
    }

    public String getJarPath() {
        return jarPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TreeNodeFileDTO(Type type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }

    public TreeNodeFileDTO(Type type, String jarPath, String displayName) {
        this.type = type;
        this.jarPath = jarPath;
        this.displayName = displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
