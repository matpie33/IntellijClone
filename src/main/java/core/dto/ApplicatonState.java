package core.dto;

import org.springframework.stereotype.Component;

@Component
public class ApplicatonState {

    private String projectPath;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
}
