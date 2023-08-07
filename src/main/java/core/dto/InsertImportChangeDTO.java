package core.dto;

public class InsertImportChangeDTO extends InsertChangeDTO{

    private String fullyQualifiedClassName;

    public InsertImportChangeDTO(int offsetWhereChangeStarted, String fullyQualifiedClassName) {
        super(offsetWhereChangeStarted);
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }
}
