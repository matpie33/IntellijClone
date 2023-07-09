package core.dto;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class RenamedFileDTO {

    private File file;

    private String newName;

    private DefaultMutableTreeNode node;

    public RenamedFileDTO(File file, String newName, DefaultMutableTreeNode node) {
        this.file = file;
        this.newName = newName;
        this.node = node;
    }


    public DefaultMutableTreeNode getNode() {
        return node;
    }

    public File getFile() {
        return file;
    }

    public String getNewName() {
        return newName;
    }
}
