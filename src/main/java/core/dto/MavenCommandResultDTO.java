package core.dto;

import java.io.File;

public class MavenCommandResultDTO {

    private boolean success;

    private String output;

    private File outputFile;

    public MavenCommandResultDTO(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }
}
