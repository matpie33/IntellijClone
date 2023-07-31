package core.dto;

import java.io.File;

public class JDKPathValidationDTO {

    private File selectedFile;

    private boolean pathValid;

    public JDKPathValidationDTO(File selectedFile, boolean pathValid) {
        this.selectedFile = selectedFile;
        this.pathValid = pathValid;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public boolean isPathValid() {
        return pathValid;
    }
}
