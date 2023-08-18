package root.core.projectmanipulating;

import org.springframework.stereotype.Component;
import root.core.configuration.ConfigurationExtractor;
import root.core.configuration.ConfigurationHolderType;

import java.nio.file.Path;

@Component
public class LastOpenedProjectConfigurationExtractor implements ConfigurationExtractor<String> {

    private static final String CONFIG_KEY = "recentProject: ";

    private String recentProject = "";

    private ProjectOpener projectOpener;

    public LastOpenedProjectConfigurationExtractor(ProjectOpener projectOpener) {
        this.projectOpener = projectOpener;
    }


    @Override
    public String getConfigKey() {
        return CONFIG_KEY;
    }

    @Override
    public boolean isNewValue() {
        return recentProject.isEmpty();
    }

    @Override
    public ConfigurationHolderType getType() {
        return ConfigurationHolderType.RECENT_PROJECTS;
    }

    @Override
    public void extractConfigValue(String configurationValue){
        this.recentProject = configurationValue;
        projectOpener.openProject(Path.of(recentProject).toFile());

    }

    @Override
    public void saveValue(String recentProject) {
        this.recentProject = recentProject;
    }

    @Override
    public String createConfigValue() {
        return recentProject;
    }

    @Override
    public String getValue() {
        return recentProject;
    }

    @Override
    public void handleConfigValueNotPresent() {

    }
}
