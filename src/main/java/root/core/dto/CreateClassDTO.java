package root.core.dto;

import root.core.constants.ClassType;

public class CreateClassDTO {

    private String className;
    private ClassType classType;

    public CreateClassDTO(String className, ClassType classType) {
        this.className = className;
        this.classType = classType;
    }

    public String getClassName() {
        return className;
    }

    public ClassType getClassType() {
        return classType;
    }
}
