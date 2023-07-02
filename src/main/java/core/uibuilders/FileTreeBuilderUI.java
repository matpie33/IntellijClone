package core.uibuilders;

import core.dto.DirectoryDTO;
import core.dto.FileDTO;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.List;

@Component
public class FileTreeBuilderUI {

    public JTree build (File root, List<FileDTO> children){
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode(root.getName());
        JTree tree = new JTree(top);
        for (FileDTO child : children) {
            addNode(top, child);
        }
        return tree;

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
