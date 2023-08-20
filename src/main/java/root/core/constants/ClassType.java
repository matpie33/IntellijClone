package root.core.constants;

import java.util.Arrays;

public enum ClassType {
    CLASS("Class"), INTERFACE("Interface"), ANNOTATION("Annotation"), ENUM ("Enum");

    private String uiValue;

    ClassType(String uiValue) {
        this.uiValue = uiValue;
    }

    public static ClassType fromUiValue(String uiValue){
        return Arrays.stream(values()).filter(enumValue->enumValue.uiValue.equals(uiValue)).findFirst().orElseThrow();
    }


}
