package root.core.configuration;

import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ConfigurationHolder {


    public static final String CONFIG_FILENAME = "config.txt";
    public static final String CONFIG_DIRECTORY_NAME = "intellijClone";
    private File configFile;
    private final UIEventsQueue uiEventsQueue;
    private final List<ConfigurationExtractor<?>> configurationExtractors;

    public ConfigurationHolder(List<ConfigurationExtractor<?>> configurationExtractors, UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
        this.configurationExtractors = configurationExtractors;
    }

    public void init () throws IOException {
        File configFile = createConfigFile();
        List<String> configContents = Files.readAllLines(configFile.toPath());
        Set<ConfigurationExtractor<?>> usedExtractors = new HashSet<>();
        for (String configurationLine : configContents) {
            for (ConfigurationExtractor<?> extractor : configurationExtractors) {
                String configKey = extractor.getConfigKey();
                if (configurationLine.contains(configKey)){
                    usedExtractors.add(extractor);
                    String configValue = configurationLine.replace(configKey, "");
                    extractor.extractConfigValue(configValue);
                }
            }
        }
        if (usedExtractors.size() != configurationExtractors.size()){
            for (ConfigurationExtractor<?> extractor : configurationExtractors) {
                if (!usedExtractors.contains(extractor)){
                    extractor.handleConfigValueNotPresent();
                }
            }
        }
    }

    public String getJdkPath (){
        return (String)getExtractorOfType(ConfigurationHolderType.JDK).getValue();
    }

    public <T> void saveConfiguration (String configurationValue, ConfigurationHolderType configurationHolderType){

        try {
            ConfigurationExtractor<T> configurationExtractor = getExtractorOfType(configurationHolderType);
            boolean isNewValue = configurationExtractor.isNewValue();
            configurationExtractor.saveValue(configurationValue);
            List<String> contentLines = Files.readAllLines(configFile.toPath());
            String configValue = configurationExtractor.createConfigValue();
            String configKey = configurationExtractor.getConfigKey();
            String entry = configKey + configValue + "\n";
            if (isNewValue){
                Files.write(configFile.toPath(), entry.getBytes(), StandardOpenOption.APPEND);
                uiEventsQueue.dispatchEvent(UIEventType.APPLICATION_MESSAGE_ARRIVED, "");
            }
            else{
                String configLine = contentLines.stream().filter(line -> line.contains(configKey)).findFirst().orElseThrow();
                int indexInList = contentLines.indexOf(configLine);
                contentLines.remove(configLine);
                contentLines.add( indexInList, entry);
                String newContent = String.join("\n", contentLines);
                PrintWriter prw= new PrintWriter(configFile);
                prw.print(newContent);
                prw.close();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private <T> ConfigurationExtractor<T> getExtractorOfType(ConfigurationHolderType configurationHolderType) {
        return (ConfigurationExtractor<T>) configurationExtractors.stream().filter(extractor -> extractor.getType().equals(configurationHolderType)).findFirst().orElseThrow();
    }

    private File createConfigFile() throws IOException {
        Path configDirectory = Path.of(System.getenv("APPDATA")).resolve(CONFIG_DIRECTORY_NAME);
        configDirectory.toFile().mkdir();
        configFile = configDirectory.resolve(CONFIG_FILENAME).toFile();
        configFile.createNewFile();
        return configFile;
    }

}
