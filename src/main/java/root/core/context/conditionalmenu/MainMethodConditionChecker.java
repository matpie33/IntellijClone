package root.core.context.conditionalmenu;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicatonState;
import root.core.dto.ProjectStructureSelectionContextDTO;

import java.io.File;

@Component
public class MainMethodConditionChecker implements ConditionChecker<ProjectStructureSelectionContextDTO>{

    private ApplicatonState applicatonState;

    public MainMethodConditionChecker(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    @Override
    public boolean isConditionFulfilled(ProjectStructureSelectionContextDTO contextDTO) {
        File file = contextDTO.getSelectedFile();
        return applicatonState.getClassesWithMainMethod().contains(file);
    }
}
