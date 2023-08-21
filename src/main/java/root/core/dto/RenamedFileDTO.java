package root.core.dto;

import root.core.ui.tree.ProjectStructureNode;

import java.io.File;

public class RenamedFileDTO {

    private File file;

    private String newName;

    private ProjectStructureNode node;

    public RenamedFileDTO(File file, String newName, ProjectStructureNode node) {
        this.file = file;
        this.newName = newName;
        this.node = node;
    }


    public ProjectStructureNode getNode() {
        return node;
    }

    public File getFile() {
        return file;
    }

    public String getNewName() {
        return newName;
    }
}
