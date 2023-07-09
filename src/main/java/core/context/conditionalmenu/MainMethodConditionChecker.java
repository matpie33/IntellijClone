package core.context.conditionalmenu;

import core.dto.ApplicatonState;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

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
