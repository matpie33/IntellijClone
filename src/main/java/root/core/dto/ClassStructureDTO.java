package root.core.dto;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import root.core.constants.ClassType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassStructureDTO {

    private ClassType classType;

    private List<Range> fieldAccessPositions = new ArrayList<>();

    private List<Range> commentsSections = new ArrayList<>();

    private Position packageDeclarationPosition;

    private Position classDeclarationPosition;

    private Set<String> imports = new HashSet<>();

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public void addCommentRange(Range range){
        Range modifiedRange = new Range(new Position(range.begin.line - 1, range.begin.column - 1),
                new Position(range.end.line - 1, range.end.column - 1));
        commentsSections.add(modifiedRange);
    }

    public List<Range> getCommentsSections() {
        return commentsSections;
    }

    public Position getClassDeclarationPosition() {
        return classDeclarationPosition;
    }

    public void setClassDeclarationPosition(Position classDeclarationPosition) {
        this.classDeclarationPosition = classDeclarationPosition;
    }

    public Position getPackageDeclarationPosition() {
        return packageDeclarationPosition;
    }

    public void addImport (String importName){
        imports.add(importName);
    }

    public boolean containsImport (String importName ){
        return imports.contains(importName);
    }

    public void setPackageDeclarationPosition(Position packageDeclarationPosition) {
        this.packageDeclarationPosition = packageDeclarationPosition;
    }

    public void addFieldAccess(Range tokenRange){
        Position start = new Position(tokenRange.begin.line-1, tokenRange.begin.column-1);
        Position end = new Position(tokenRange.end.line-1, tokenRange.end.column-1);
        Range modifiedRange = new Range(start, end);
        fieldAccessPositions.add(modifiedRange);
    }

    public List<Range> getFieldAccessPositions(){
        return fieldAccessPositions;

    }

    public void removeImport(String fullyQualifiedClassName) {
        imports.remove(fullyQualifiedClassName);
    }
}
