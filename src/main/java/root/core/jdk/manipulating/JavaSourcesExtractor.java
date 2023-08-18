package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;
import root.core.configuration.ConfigurationHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class JavaSourcesExtractor {

    private static final String LIB = "lib";
    private static final String SRC = "src.zip";

    private static final String DESTINATION_DIRECTORY = ConfigurationHolder.CONFIG_DIRECTORY_NAME + "/javasrc";

    public void extractSources (String pathToJDK) throws IOException {
        Path pathToJDKObject     = Path.of(pathToJDK);
        Path librariesDirectory = pathToJDKObject.resolve(LIB);
        Path sourcesZipPath = librariesDirectory.resolve(SRC);
        Path destinationDirectory = getJavaSourcesDirectory();
        File destinationDirectoryFile = destinationDirectory.toFile();
        if (destinationDirectoryFile.exists() && destinationDirectoryFile.listFiles().length>0){
            return;
        }
        File zipFile = sourcesZipPath.toFile();
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = inputStream.getNextEntry();
        while (zipEntry != null){
            File fileInsideZIP = new File(destinationDirectoryFile, zipEntry.getName());
            if (zipEntry.isDirectory()){
                boolean didCreateDirectory = fileInsideZIP.mkdirs();
                if (!didCreateDirectory){
                    throw new RuntimeException("failed to create directory");
                }
            }
            else{
                File parent = fileInsideZIP.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()){
                    throw new RuntimeException("failed to create directory");
                }
                Files.write(fileInsideZIP.toPath(), inputStream.readAllBytes());
            }
            zipEntry = inputStream.getNextEntry();
        }


    }

    public Path getJavaSourcesDirectory() {
        return Path.of(System.getenv("APPDATA")).resolve(DESTINATION_DIRECTORY);
    }



}
