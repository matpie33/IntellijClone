package core.dto;

public class ClassSugestionDTO {

    private String className;
    private String packageName;

    public ClassSugestionDTO(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return className + ": "+packageName;
    }
}
