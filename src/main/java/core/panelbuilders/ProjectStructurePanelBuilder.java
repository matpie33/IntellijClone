package core.panelbuilders;

import core.backend.DirectoryChangesDetector;
import core.backend.FileIO;
import core.context.ContextConfiguration;
import core.context.providers.NodePathManipulation;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileSystemChangeDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.dto.RenamedFileDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.mouselisteners.TreeNodeDoubleClickListener;
import core.shortcuts.ProjectStructureTreeShortcuts;
import core.uibuilders.ProjectStructureBuilderUI;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectStructurePanelBuilder implements UIEventObserver {

    private JTree projectStructureTree;

    private JPanel projectStructurePanel;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;

    private ContextConfiguration contextConfiguration;



    private DirectoryChangesDetector directoryChangesDetector;

    private NodePathManipulation nodePathManipulation;

    private ProjectStructureTreeShortcuts projectStructureTreeShortcuts;


    private ProjectStructureBuilderUI projectStructureBuilderUI;

    private FileIO fileIO;

    private ApplicatonState applicatonState;

    public ProjectStructurePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextConfiguration contextConfiguration, DirectoryChangesDetector directoryChangesDetector, NodePathManipulation nodePathManipulation, ProjectStructureTreeShortcuts projectStructureTreeShortcuts, ProjectStructureBuilderUI projectStructureBuilderUI, FileIO fileIO, ApplicatonState applicatonState) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextConfiguration = contextConfiguration;
        this.directoryChangesDetector = directoryChangesDetector;
        this.nodePathManipulation = nodePathManipulation;
        this.projectStructureTreeShortcuts = projectStructureTreeShortcuts;
        this.projectStructureBuilderUI = projectStructureBuilderUI;
        this.fileIO = fileIO;
        this.applicatonState = applicatonState;
    }

    @PostConstruct
    public void init (){
        projectStructurePanel = new JPanel(new BorderLayout());
        projectStructureTree = new JTree(new DefaultMutableTreeNode("Empty"));
        projectStructureTree.addMouseListener(new PopupMenuRequestListener(ContextType.PROJECT_STRUCTURE, contextConfiguration));
        projectStructureTree.addMouseListener(treeNodeDoubleClickListener);
        projectStructureTree.addMouseListener(directoryChangesDetector);
        projectStructurePanel.add(new JScrollPane(projectStructureTree));
        projectStructureTreeShortcuts.assignShortcuts(projectStructureTree);
    }

    public JPanel getPanel() {
        return projectStructurePanel;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        DefaultTreeModel model = (DefaultTreeModel) projectStructureTree.getModel();
        DefaultMutableTreeNode rootNode;
        switch (eventType) {
            case PROJECT_OPENED:
                rootNode = (DefaultMutableTreeNode) data;
                model.setRoot(rootNode);
                projectStructurePanel.revalidate();
                break;
            case FILE_REMOVED_FROM_PROJECT:
                ProjectStructureSelectionContextDTO context = (ProjectStructureSelectionContextDTO) data;
                TreePath[] selectedPaths = context.getSelectedPaths();
                for (TreePath selectedPath : selectedPaths) {
                    model.removeNodeFromParent((MutableTreeNode) selectedPath.getLastPathComponent());
                }
                projectStructurePanel.revalidate();
                break;
            case PROJECT_STRUCTURE_CHANGED:
                FileSystemChangeDTO fileSystemChangeDTO = (FileSystemChangeDTO) data;
                rootNode = (DefaultMutableTreeNode) projectStructureTree.getModel().getRoot();
                nodePathManipulation.updateTreeStructure(fileSystemChangeDTO, rootNode, model);
                break;
            case FILENAME_CHANGED:
                RenamedFileDTO renamedFileDTO = (RenamedFileDTO) data;
                FileIO.RenameResult renameResult = fileIO.renameFile(renamedFileDTO);
                if (!renameResult.isSuccess()){
                    System.err.println("Failed to rename file");
                }
                else{
                    projectStructureBuilderUI.renameNode(renamedFileDTO.getNode(), renamedFileDTO.getNewName());
                    model.nodeChanged(renamedFileDTO.getNode());
                }
        }
    }

}
