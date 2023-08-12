package root.core.dto;

public class MavenCommandResultDTO {

    private boolean isSuccess;

    private String output;

    public MavenCommandResultDTO(boolean isSuccess, String output) {
        this.isSuccess = isSuccess;
        this.output = output;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getOutput() {
        return output;
    }
}
