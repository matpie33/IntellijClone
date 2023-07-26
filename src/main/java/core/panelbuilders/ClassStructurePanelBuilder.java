package core.panelbuilders;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.nodehandling.ClassStructureNodeClickListener;
import core.uibuilders.ClassStructureNodesHandler;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.FileNotFoundException;

@Component
public class ClassStructurePanelBuilder implements UIEventObserver {

    private JPanel classStructurePanel;

    private JTree classStructureTree;

    private ClassStructureNodeClickListener classStructureNodeClickListener;

    private ClassStructureNodesHandler classStructureNodesHandler;

    private ApplicatonState applicatonState;

    private ContextConfiguration contextConfiguration;

    public ClassStructurePanelBuilder(ClassStructureNodeClickListener classStructureNodeClickListener, ClassStructureNodesHandler classStructureNodesHandler, ApplicatonState applicatonState, ContextConfiguration contextConfiguration) {
        this.classStructureNodeClickListener = classStructureNodeClickListener;
        this.classStructureNodesHandler = classStructureNodesHandler;
        this.applicatonState = applicatonState;
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
                if (resultDTO.isJavaFile()){
                    displayJavaFileStructure();
                }
                else{
                    DefaultTreeModel structureModel = (DefaultTreeModel) classStructureTree.getModel();
                    structureModel.setRoot(classStructureNodesHandler.createEmptyRootNode());
                }
                break;
            case COMPILATION_ERROR_FIXED_IN_OPENED_FILE:
                displayJavaFileStructure();
                break;
        }
    }

    private void displayJavaFileStructure() {
        try {
            DefaultTreeModel structureModel = (DefaultTreeModel) classStructureTree.getModel();
            CompilationUnit compilationUnit = StaticJavaParser.parse(applicatonState.getOpenedFile());
            DefaultMutableTreeNode rootNode = classStructureNodesHandler.build(compilationUnit.getType(0));
            structureModel.setRoot(rootNode);
            classStructurePanel.revalidate();
        } catch (FileNotFoundException|ParseProblemException e) {
            applicatonState.addClassWithCompilationError(applicatonState.getOpenedFile());
            e.printStackTrace();
        }
    }

}
