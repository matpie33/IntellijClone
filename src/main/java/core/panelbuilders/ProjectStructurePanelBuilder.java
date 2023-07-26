package core.panelbuilders;

import core.backend.DirectoryChangesDetector;
import core.backend.FileIO;
import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.FileSystemChangeDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.dto.RenamedFileDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.nodehandling.TreeNodeDoubleClickListener;
import core.shortcuts.ProjectStructureTreeShortcuts;
import core.uibuilders.ProjectStructureNodesHandler;
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
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Component
public class ProjectStructurePanelBuilder implements UIEventObserver {

    private JTree projectStructureTree;

    private JPanel projectStructurePanel;

    private TreeNodeDoubleClickListener treeNodeDoubleClickListener;

    private ContextConfiguration contextConfiguration;



    private DirectoryChangesDetector directoryChangesDetector;

    private ProjectStructureTreeShortcuts projectStructureTreeShortcuts;


    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private FileIO fileIO;

    public ProjectStructurePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextConfiguration contextConfiguration, DirectoryChangesDetector directoryChangesDetector, ProjectStructureTreeShortcuts projectStructureTreeShortcuts, ProjectStructureNodesHandler projectStructureNodesHandler, FileIO fileIO) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextConfiguration = contextConfiguration;
        this.directoryChangesDetector = directoryChangesDetector;
        this.projectStructureTreeShortcuts = projectStructureTreeShortcuts;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.fileIO = fileIO;
    }

    @PostConstruct
    public void init (){
        projectStructurePanel = new JPanel(new BorderLayout());
        projectStructureTree = new JTree(projectStructureNodesHandler.createEmptyRootNode());
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
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        switch (eventType) {
            case MAVEN_CLASSPATH_READED:
                Map<String, List<File>> jarToClassesMap = (Map<String, List<File>>) data;
                projectStructureNodesHandler.addExternalDependencies(model, jarToClassesMap, rootNode);
                break;
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
                projectStructureNodesHandler.updateTreeStructure(fileSystemChangeDTO, rootNode, model);
                break;
            case FILENAME_CHANGED:
                RenamedFileDTO renamedFileDTO = (RenamedFileDTO) data;
                FileIO.RenameResult renameResult = fileIO.renameFile(renamedFileDTO);
                if (!renameResult.isSuccess()){
                    System.err.println("Failed to rename file");
                }
                else{
                    projectStructureNodesHandler.renameNode(renamedFileDTO.getNode(), renamedFileDTO.getNewName());
                    model.nodeChanged(renamedFileDTO.getNode());
                }
        }
    }

}
