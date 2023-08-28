package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.classmanipulating.ClassStructureParser;
import root.core.context.ContextConfiguration;
import root.core.context.contextMenu.ContextType;
import root.core.directory.changesdetecting.DirectoryChangesDetector;
import root.core.dto.FileDTO;
import root.core.dto.FileSystemChangeDTO;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.RenamedFileDTO;
import root.core.fileio.FileIO;
import root.core.jdk.manipulating.JavaSourcesExtractor;
import root.core.mouselisteners.PopupMenuRequestListener;
import root.core.nodehandling.ProjectStructureNodesHandler;
import root.core.nodehandling.TreeNodeDoubleClickListener;
import root.core.shortcuts.ProjectStructureTreeShortcuts;
import root.core.ui.tree.ProjectStructureModel;
import root.core.ui.tree.ProjectStructureNode;
import root.core.ui.tree.ProjectStructureTreeNodeRenderer;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.core.utility.ThreadExecutor;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
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

    private JavaSourcesExtractor javaSourcesExtractor;

    private ThreadExecutor threadExecutor;

    private ClassStructureParser classStructureParser;


    public static final int CLASSES_TO_PARSE_PER_THREAD = 50;

    public ProjectStructurePanelBuilder(TreeNodeDoubleClickListener treeNodeDoubleClickListener, ContextConfiguration contextConfiguration, DirectoryChangesDetector directoryChangesDetector, ProjectStructureTreeShortcuts projectStructureTreeShortcuts, ProjectStructureNodesHandler projectStructureNodesHandler, FileIO fileIO, JavaSourcesExtractor javaSourcesExtractor, ThreadExecutor threadExecutor, ClassStructureParser classStructureParser) {
        this.treeNodeDoubleClickListener = treeNodeDoubleClickListener;
        this.contextConfiguration = contextConfiguration;
        this.directoryChangesDetector = directoryChangesDetector;
        this.projectStructureTreeShortcuts = projectStructureTreeShortcuts;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.fileIO = fileIO;
        this.javaSourcesExtractor = javaSourcesExtractor;
        this.threadExecutor = threadExecutor;
        this.classStructureParser = classStructureParser;
    }

    @PostConstruct
    public void init (){
        projectStructurePanel = new JPanel(new BorderLayout());
        projectStructureTree = new JTree(new ProjectStructureModel(projectStructureNodesHandler.createEmptyRootNode()));
        projectStructureTree.setCellRenderer(new ProjectStructureTreeNodeRenderer());
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
        ProjectStructureModel model = (ProjectStructureModel) projectStructureTree.getModel();
        final ProjectStructureNode rootNode = (ProjectStructureNode) model.getRoot();
        switch (eventType) {
            case MAVEN_CLASSPATH_READED:
                Map<String, List<FileDTO>> jarToClassesMap = (Map<String, List<FileDTO>>) data;
                SwingUtilities.invokeLater(()->projectStructureNodesHandler.addExternalDependencies(model, jarToClassesMap, rootNode));
                break;
            case PROJECT_OPENED:
                File jdkSourcesRoot = javaSourcesExtractor.getJavaSourcesDirectory().toFile();
                parseJdkSources(jdkSourcesRoot);
                File rootDirectory = (File) data;
                ProjectStructureNode localRoot = projectStructureNodesHandler.addNodesForSources(model, rootDirectory, ClassOrigin.SOURCES, true);
                projectStructureNodesHandler.addNodesForJDKSources(model, localRoot, jdkSourcesRoot);
                model.setRoot(localRoot);
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
                SwingUtilities.invokeLater(()->projectStructureNodesHandler.updateTreeStructure(fileSystemChangeDTO, rootNode, model));
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

    private void groupClassesToParseByThread(File jdkSourcesRoot, int groupSize, List<File> fileGroup) {
        for (File file : jdkSourcesRoot.listFiles()) {
            if (file.isFile()){
                if (fileGroup.size()< groupSize){
                    fileGroup.add(file);
                }
                else{
                    ArrayList<File> finalList = new ArrayList<>(fileGroup);
                    threadExecutor.runTaskInMainPoolAfterMavenTaskDone(()-> parseClasses(finalList, ClassOrigin.JDK));
                    fileGroup.clear();
                    fileGroup.add(file);
                }
            }
            else{
                groupClassesToParseByThread(file, groupSize, fileGroup);
            }

        }
    }

    private void parseClasses(List<File> classesGroup, ClassOrigin origin) {
        for (File classFile : classesGroup) {
            classStructureParser.parseClassStructure(classFile, origin);
        }
    }

    private void parseJdkSources(File jdkSourcesRoot) {

        List<File> classesGroup = new ArrayList<>();
        groupClassesToParseByThread(jdkSourcesRoot, CLASSES_TO_PARSE_PER_THREAD, classesGroup);
        if (!classesGroup.isEmpty()){
            final ArrayList<File> files = new ArrayList<>(classesGroup);
            threadExecutor.runTaskInJdkPoolAfterMavenTaskDone(()-> parseClasses(files, ClassOrigin.JDK));
        }
    }

}
