package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.context.ContextConfiguration;
import root.core.context.contextMenu.ContextType;
import root.core.dto.ApplicationState;
import root.core.dto.ClassStructureDTO;
import root.core.dto.FileReadResultDTO;
import root.core.mouselisteners.PopupMenuRequestListener;
import root.core.nodehandling.ClassStructureNodeClickListener;
import root.core.nodehandling.ClassStructureNodesHandler;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;

@Component
public class ClassStructurePanelBuilder implements UIEventObserver {

    private JPanel classStructurePanel;

    private JTree classStructureTree;

    private ClassStructureNodeClickListener classStructureNodeClickListener;

    private ClassStructureNodesHandler classStructureNodesHandler;

    private ApplicationState applicationState;

    private ContextConfiguration contextConfiguration;

    public ClassStructurePanelBuilder(ClassStructureNodeClickListener classStructureNodeClickListener, ClassStructureNodesHandler classStructureNodesHandler, ApplicationState applicationState, ContextConfiguration contextConfiguration) {
        this.classStructureNodeClickListener = classStructureNodeClickListener;
        this.classStructureNodesHandler = classStructureNodesHandler;
        this.applicationState = applicationState;
        this.contextConfiguration = contextConfiguration;
    }

    public JPanel getPanel (){
        return classStructurePanel;
    }

    @PostConstruct
    public void init (){
        classStructurePanel = new JPanel(new BorderLayout());
        classStructureTree = new JTree(classStructureNodesHandler.createEmptyRootNode()){
            @Override
            protected void setExpandedState(TreePath path, boolean state) {

            }
        };
        classStructureTree.addMouseListener(classStructureNodeClickListener);
        classStructureTree.addMouseListener(new PopupMenuRequestListener(ContextType.CLASS_STRUCTURE, contextConfiguration));
        classStructurePanel.add(new JScrollPane(classStructureTree), BorderLayout.CENTER);
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case FILE_OPENED_FOR_EDIT:

                FileReadResultDTO resultDTO = (FileReadResultDTO)data;
                ClassOrigin classOrigin = resultDTO.getClassOrigin();
                if (classOrigin.equals(ClassOrigin.JDK) || classOrigin.equals(ClassOrigin.SOURCES) || classOrigin.equals(ClassOrigin.MAVEN)){
                    displayJavaFileStructure(resultDTO.getFile());
                }
                else{
                    DefaultTreeModel structureModel = (DefaultTreeModel) classStructureTree.getModel();
                    structureModel.setRoot(classStructureNodesHandler.createEmptyRootNode());
                }
                break;
            case COMPILATION_ERROR_FIXED_IN_OPENED_FILE:
                File file = (File)data;
                displayJavaFileStructure(file);
                break;
        }
    }

    private void displayJavaFileStructure(File file) {
        DefaultTreeModel treeModel = (DefaultTreeModel) classStructureTree.getModel();
        ClassStructureDTO classStructure = applicationState.getClassStructure(file);
        if (classStructure == null){
            return;
        }
        DefaultMutableTreeNode rootNode = classStructureNodesHandler.build(classStructure);
        treeModel.setRoot(rootNode);
        classStructurePanel.revalidate();
    }

}
