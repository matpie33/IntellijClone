package root.core.dto;

public class MethodDeclarationDTO  extends FieldDeclarationDTO{

    private String parameters = "";

    public MethodDeclarationDTO(int startOffset, String tokenNames, String modifiers, String type) {
        super(startOffset, tokenNames, modifiers, type);
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
