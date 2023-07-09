package core.uibuilders;

import core.dto.DirectoryDTO;
import core.dto.FileDTO;
import org.springframework.stereotype.Component;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.List;

@Component
public class ProjectStructureBuilderUI {

    public void renameNode (DefaultMutableTreeNode node, String newFileName){
        node.setUserObject(newFileName);
    }

    public DefaultMutableTreeNode build (File root, List<FileDTO> children){
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(root.getName());
        for (FileDTO child : children) {
            addNode(top, child);
        }
        return top;

    }

    private void addNode(DefaultMutableTreeNode parentNode, FileDTO child) {
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(child.getName());
        parentNode.add(fileNode);
        if (child instanceof DirectoryDTO){
            extractChildren(fileNode, (DirectoryDTO)child);
        }
    }

    private void extractChildren(DefaultMutableTreeNode parentNode, DirectoryDTO directoryDTO) {
        for (FileDTO child : directoryDTO.getFiles()) {
            addNode(parentNode, child);
        }
    }

}
