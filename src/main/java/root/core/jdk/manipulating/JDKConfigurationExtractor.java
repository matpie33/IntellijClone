package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;
import root.core.configuration.ConfigurationExtractor;
import root.core.configuration.ConfigurationHolderType;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.IOException;

@Component
public class JDKConfigurationExtractor implements ConfigurationExtractor<String> {

    public static final String JDK_PATH_KEY = "jdkPath: ";

    public static final String JDK_PATH_NOT_SET_MESSAGE = "JDK path not set. Open settings to set the path.";

    private JavaSourcesExtractor javaSourcesExtractor;

    private String pathToJDK;

    private UIEventsQueue uiEventsQueue;

    public JDKConfigurationExtractor(JavaSourcesExtractor javaSourcesExtractor, UIEventsQueue uiEventsQueue) {
        this.javaSourcesExtractor = javaSourcesExtractor;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public String getConfigKey(){
        return JDK_PATH_KEY;
    }

    @Override
    public boolean isNewValue(){
        return pathToJDK == null;
    }

    @Override
    public ConfigurationHolderType getType(){
        return ConfigurationHolderType.JDK;
    }


    @Override
    public void saveValue(String configValue){
        try {
            javaSourcesExtractor.extractSources(configValue);
            this.pathToJDK = configValue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void extractConfigValue(String configurationValue) {
        try {
            javaSourcesExtractor.extractSources(configurationValue);
            this.pathToJDK = configurationValue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String createConfigValue(){
        return pathToJDK;
    }


    @Override
    public String getValue() {
        return pathToJDK;
    }

    @Override
    public void handleConfigValueNotPresent() {
        uiEventsQueue.dispatchEvent(UIEventType.APPLICATION_MESSAGE_ARRIVED, JDK_PATH_NOT_SET_MESSAGE);
    }
}
