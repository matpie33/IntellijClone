package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

@Component
public class JDKConfigurationHolder {

    public static final String JDK_PATH_KEY = "jdkPath: ";
    public static final String CONFIG_FILENAME = "config.txt";
    public static final String CONFIG_DIRECTORY_NAME = "intellijClone";
    public static final String JDK_PATH_NOT_SET_MESSAGE = "JDK path not set. Open settings to set the path.";
    private File configFile;
    private String pathToJDK;

    private UIEventsQueue uiEventsQueue;

    private JavaSourcesExtractor javaSourcesExtractor;

    public JDKConfigurationHolder(UIEventsQueue uiEventsQueue, JavaSourcesExtractor javaSourcesExtractor) {
        this.uiEventsQueue = uiEventsQueue;
        this.javaSourcesExtractor = javaSourcesExtractor;
    }

    public void init () throws IOException {
        File configFile = createConfigFile();
        List<String> contentLines = Files.readAllLines(configFile.toPath());
        Optional<String> jdkPathLine = contentLines.stream().filter(line -> line.contains(JDK_PATH_KEY)).findFirst();
        if (jdkPathLine.isPresent()){
            String jdkPathKeyValue = jdkPathLine.get();
            pathToJDK = jdkPathKeyValue.replace(JDK_PATH_KEY, "");
            javaSourcesExtractor.extractSources(pathToJDK);
        }
        else{
            uiEventsQueue.dispatchEvent(UIEventType.APPLICATION_MESSAGE_ARRIVED, JDK_PATH_NOT_SET_MESSAGE);
        }

    }

    public String getJdkPath (){
        return pathToJDK != null? pathToJDK: "";
    }

    public void saveConfiguration (String path){

        try {
            List<String> contentLines = Files.readAllLines(configFile.toPath());
            if (pathToJDK == null){
                boolean addNewLine = !contentLines.isEmpty();
                String jdkEntry =(addNewLine? "\n" : "") + createJdkEntry(path);
                Files.write(configFile.toPath(), jdkEntry.getBytes(), StandardOpenOption.APPEND);
                pathToJDK = path;
                uiEventsQueue.dispatchEvent(UIEventType.APPLICATION_MESSAGE_ARRIVED, "");

            }
            else{
                String jdkLine = contentLines.stream().filter(line -> line.contains(pathToJDK)).findFirst().orElseThrow();
                int indexInList = contentLines.indexOf(jdkLine);
                contentLines.remove(jdkLine);
                jdkLine = jdkLine.replace(pathToJDK, path);
                contentLines.add( indexInList, jdkLine);
                pathToJDK = path;
                Files.write(configFile.toPath(), String.join("\n", contentLines).getBytes(),
                        StandardOpenOption.WRITE);
            }
            javaSourcesExtractor.extractSources(pathToJDK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String createJdkEntry (String value){
        return JDK_PATH_KEY + value;
    }

    private File createConfigFile() throws IOException {
        Path configDirectory = Path.of(System.getenv("APPDATA")).resolve(CONFIG_DIRECTORY_NAME);
        configDirectory.toFile().mkdir();
        configFile = configDirectory.resolve(CONFIG_FILENAME).toFile();
        configFile.createNewFile();
        return configFile;
    }

}
