package root.core.context.conditionalmenu;

import org.springframework.stereotype.Component;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.dto.ProjectStructureTreeElementDTO;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

@Component
public class FileCreateConditionChecker implements ConditionChecker<ProjectStructureSelectionContextDTO>{


    @Override
    public boolean isConditionFulfilled(ProjectStructureSelectionContextDTO contextDTO) {
        TreePath[] selectedPaths = contextDTO.getSelectedPaths();
        if (selectedPaths.length!=1){
            return false;
        }
        TreePath selectedPath = selectedPaths[0];
        for (int i = 0; i < selectedPath.getPathCount(); i++) {
            DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) selectedPath.getPathComponent(i);
            ProjectStructureTreeElementDTO treeElement = (ProjectStructureTreeElementDTO) pathComponent.getUserObject();
            if (treeElement.getDisplayName().equals("JDK") || treeElement.getDisplayName().equals("maven")){
                return false;
            }
        }
        return true;
    }
}
