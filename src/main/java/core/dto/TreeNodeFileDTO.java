package core.dto;

import java.util.Objects;

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

    @Override
    public boolean equals(Object objectInstance) {
        if (this == objectInstance) return true;
        if (objectInstance == null || getClass() != objectInstance.getClass()) return false;
        TreeNodeFileDTO that = (TreeNodeFileDTO) objectInstance;
        return type == that.type && Objects.equals(displayName, that.displayName) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, displayName, path);
    }
}
