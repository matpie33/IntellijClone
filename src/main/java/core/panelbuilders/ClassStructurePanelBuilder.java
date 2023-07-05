package core.panelbuilders;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import core.dto.ApplicatonState;
import core.dto.FileReadResultDTO;
import core.mouselisteners.ClassStructureNodeClickListener;
import core.uibuilders.ClassStructureBuilderUI;
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

    private ClassStructureBuilderUI classStructureBuilderUI;

    private ApplicatonState applicatonState;

    public ClassStructurePanelBuilder(ClassStructureNodeClickListener classStructureNodeClickListener, ClassStructureBuilderUI classStructureBuilderUI, ApplicatonState applicatonState) {
        this.classStructureNodeClickListener = classStructureNodeClickListener;
        this.classStructureBuilderUI = classStructureBuilderUI;
        this.applicatonState = applicatonState;
    }

    public JPanel getPanel (){
        return classStructurePanel;
    }

    @PostConstruct
    public void init (){
        classStructurePanel = new JPanel(new BorderLayout());
        classStructureTree = new JTree(new DefaultMutableTreeNode("")){
            @Override
            protected void setExpandedState(TreePath path, boolean state) {

            }
        };
        classStructureTree.addMouseListener(classStructureNodeClickListener);
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
                    structureModel.setRoot(new DefaultMutableTreeNode("N/a"));
                }
                break;
        }
    }

    private void displayJavaFileStructure() {
        try {
            DefaultTreeModel structureModel = (DefaultTreeModel) classStructureTree.getModel();
            CompilationUnit compilationUnit = StaticJavaParser.parse(applicatonState.getOpenedFile());
            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().iterator().next();
            DefaultMutableTreeNode tree = classStructureBuilderUI.build(classDeclaration);
            structureModel.setRoot(tree);
            classStructurePanel.revalidate();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
