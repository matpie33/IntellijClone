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

    private List<TokenPositionDTO> fieldAccessPositions = new ArrayList<>();

    private List<Range> commentsSections = new ArrayList<>();

    private Position packageDeclarationPosition;

    private TokenPositionDTO classDeclarationPosition;

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

    public TokenPositionDTO getClassDeclarationPosition() {
        return classDeclarationPosition;
    }

    public void setClassDeclarationPosition(TokenPositionDTO classDeclarationPosition) {
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

    public void addFieldAccess(TokenPositionDTO tokenRange){
        fieldAccessPositions.add(tokenRange);
    }

    public List<TokenPositionDTO> getFieldAccessPositions(){
        return fieldAccessPositions;

    }

    public void removeImport(String fullyQualifiedClassName) {
        imports.remove(fullyQualifiedClassName);
    }
}
