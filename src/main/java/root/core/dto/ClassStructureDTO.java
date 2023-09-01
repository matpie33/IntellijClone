package root.core.dto;

import com.github.javaparser.Range;
import root.core.constants.ClassType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassStructureDTO {

    private ClassType classType;

    private List<TokenPositionDTO> fieldAccessPositions = new ArrayList<>();

    private List<FieldDeclarationDTO> fieldDeclarations = new ArrayList<>();

    private List<MethodDeclarationDTO> methodsDeclarations = new ArrayList<>();

    private List<Range> commentsSections = new ArrayList<>();

    private TokenPositionDTO packageDeclarationPosition;

    private ClassDeclarationDTO classDeclarationDTO;

    private Set<String> imports = new HashSet<>();

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public void addFieldDeclaration (FieldDeclarationDTO fieldDeclarationDTO){
        fieldDeclarations.add(fieldDeclarationDTO);
    }

    public void addMethodDeclaration (MethodDeclarationDTO methodDeclarationDTO){
        methodsDeclarations.add(methodDeclarationDTO);
    }

    public List<FieldDeclarationDTO> getFieldDeclarations() {
        return fieldDeclarations;
    }

    public List<Range> getCommentsSections() {
        return commentsSections;
    }

    public ClassDeclarationDTO getClassDeclaration() {
        return classDeclarationDTO;
    }

    public void setClassDeclaration(ClassDeclarationDTO classDeclarationDTO) {
        this.classDeclarationDTO = classDeclarationDTO;
    }

    public TokenPositionDTO getPackageDeclarationPosition() {
        return packageDeclarationPosition;
    }

    public void addImport (String importName){
        imports.add(importName);
    }

    public boolean containsImport (String importName ){
        return imports.contains(importName);
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

    public List<MethodDeclarationDTO> getMethodsDeclarations() {
        return methodsDeclarations;
    }

    public void setPackageDeclarationPosition(TokenPositionDTO tokenPositionDTO) {
        packageDeclarationPosition = tokenPositionDTO;
    }



}
