package core.dto;

import com.github.javaparser.Position;
import com.github.javaparser.Range;

import java.util.*;

public class ClassStructureDTO {

    private Map<Integer, List<Range>> lineNumberToFieldAccessPositions = new HashMap<>();

    private Position packageDeclarationPosition;

    private Set<String> imports = new HashSet<>();

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
        lineNumberToFieldAccessPositions.putIfAbsent(start.line, new ArrayList<>());
        lineNumberToFieldAccessPositions.get(start.line).add(modifiedRange);
    }

    public List<Range> getFieldAccessPositionsAtLine(int lineNumber){
        return lineNumberToFieldAccessPositions.getOrDefault(lineNumber, new ArrayList<>());

    }

    public void removeImport(String fullyQualifiedClassName) {
        imports.remove(fullyQualifiedClassName);
    }
}
