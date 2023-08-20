package root.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectStructureTreeElementDTO {

    public enum Type {
        CLASS_FROM_JAR, SOURCE_CLASS, DIRECTORY, EMPTY
    }

    private List<String> mergedDirectories = new ArrayList<>();

    private Type type;

    private String displayName;

    private String path;

    public String getPath() {
        return path;
    }

    public void addMergedDirectory (String directory){
        mergedDirectories.add(directory);
    }

    public List<String> getMergedDirectories() {
        return mergedDirectories;
    }

    public Type getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ProjectStructureTreeElementDTO(Type type, String displayName, String path) {
        this.type = type;
        this.displayName = displayName;
        this.path = path;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public boolean equals(Object objectInstance) {
        if (this == objectInstance) return true;
        if (objectInstance == null || getClass() != objectInstance.getClass()) return false;
        ProjectStructureTreeElementDTO that = (ProjectStructureTreeElementDTO) objectInstance;
        return type == that.type && Objects.equals(displayName, that.displayName) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, displayName, path);
    }
}
