package root.core.context.conditionalmenu;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ProjectStructureSelectionContextDTO;

import java.io.File;

@Component
public class MainMethodConditionChecker implements ConditionChecker<ProjectStructureSelectionContextDTO>{

    private ApplicationState applicationState;

    public MainMethodConditionChecker(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    @Override
    public boolean isConditionFulfilled(ProjectStructureSelectionContextDTO contextDTO) {
        File file = contextDTO.getSelectedFile();
        return applicationState.getClassesWithMainMethod().contains(file);
    }
}
