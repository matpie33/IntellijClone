package root.core.dto;

import root.core.classmanipulating.ClassOrigin;

public class ClassNavigationDTO {

    private String rootDirectory;

    private String packageName;

    private ClassOrigin origin;

    private String className;

    public ClassNavigationDTO(String rootDirectory, String packageName, ClassOrigin origin, String className) {
        this.rootDirectory = rootDirectory;
        this.packageName = packageName;
        this.origin = origin;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassOrigin getOrigin() {
        return origin;
    }
}
