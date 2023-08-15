package root.core.classmanipulating;

public enum ClassOrigin {
    SOURCES, JDK, MAVEN;

    public boolean isSourceFile(){
        return equals(JDK) ||equals(SOURCES);
    }

    public boolean isEditable (){
        return equals(SOURCES);
    }



}
