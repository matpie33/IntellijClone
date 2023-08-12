package root.core.dto;

import java.io.File;

public class JDKPathValidationDTO {

    private File selectedDirectory;

    private boolean pathValid;

    public JDKPathValidationDTO(File selectedDirectory, boolean pathValid) {
        this.selectedDirectory = selectedDirectory;
        this.pathValid = pathValid;
    }

    public File getSelectedDirectory() {
        return selectedDirectory;
    }

    public boolean isPathValid() {
        return pathValid;
    }
}
