package root.core.context.conditionalmenu;

import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.ui.tree.ProjectStructureNode;

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
            ProjectStructureNode node = (ProjectStructureNode) selectedPath.getPathComponent(i);
            if (node.getClassOrigin().equals(ClassOrigin.MAVEN) || node.getClassOrigin().equals(ClassOrigin.JDK)){
                return false;
            }
        }
        return true;
    }
}
