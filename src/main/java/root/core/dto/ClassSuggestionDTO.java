package root.core.dto;

import root.core.classmanipulating.ClassOrigin;

public class ClassSuggestionDTO {

    private String rootDirectory;
    private String className;
    private String packageName;
    private ClassOrigin classOrigin;

    public ClassSuggestionDTO(String rootDirectory, String className, String packageName, ClassOrigin classOrigin) {
        this.rootDirectory = rootDirectory;
        this.className = className;
        this.packageName = packageName;
        this.classOrigin = classOrigin;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public ClassOrigin getClassOrigin() {
        return classOrigin;
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
