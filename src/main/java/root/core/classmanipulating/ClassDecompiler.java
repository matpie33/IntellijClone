package root.core.classmanipulating;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.springframework.stereotype.Component;
import root.core.dto.FileReadResultDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class ClassDecompiler {

    public FileReadResultDTO decompile (String[] nodesPath){
        try {

            Iterator<String> nodesIterator = Arrays.stream(nodesPath).iterator();
            String pathToJarFile = nodesIterator.next();
            String pathFromJarToClass = getPathFromJarToClass(nodesIterator);
            File fileForClassContents = File.createTempFile("File", ".class");
            File tempFileDirectory = fileForClassContents.getParentFile();
            try (ZipFile zipFile = new ZipFile(pathToJarFile)) {
                createFileWithClassContents(pathFromJarToClass, fileForClassContents, zipFile);
                List<String> decompilationResult = decompile(fileForClassContents, tempFileDirectory);
                return createResultDTO(pathToJarFile, pathFromJarToClass, decompilationResult);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private FileReadResultDTO createResultDTO(String pathToJarFile, String pathFromJarToClass, List<String> decompilationResult) {
        FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
        fileReadResultDTO.setJavaFile(false);
        fileReadResultDTO.setReadSuccessfully(true);
        Path pathToFile = Path.of(pathToJarFile, pathFromJarToClass);
        String pathFromRoot = pathToFile.toString();
        fileReadResultDTO.setFile(pathToFile.toFile());
        fileReadResultDTO.setPathFromRoot(pathFromRoot);
        fileReadResultDTO.setContentLines(decompilationResult);
        fileReadResultDTO.setEditable(false);
        return fileReadResultDTO;
    }

    private List<String> decompile(File fileForClassContents, File tempFileDirectory) throws IOException {
        ConsoleDecompiler consoleDecompiler = new ConsoleDecompiler(tempFileDirectory, new HashMap<>());
        consoleDecompiler.addSpace(fileForClassContents, true);
        consoleDecompiler.decompileContext();
        File fileWithDecompilationResult = new File(fileForClassContents.toString().replace(".class", ".java"));
        return Files.readAllLines(fileWithDecompilationResult.toPath());
    }

    private void createFileWithClassContents(String pathFromJarToClass, File fileForClassContents, ZipFile zipFile) throws IOException {
        ZipEntry classFileEntry = zipFile.getEntry(pathFromJarToClass);
        InputStream inputStream = zipFile.getInputStream(classFileEntry);
        Files.write(fileForClassContents.toPath(), inputStream.readAllBytes());
    }

    private String getPathFromJarToClass(Iterator<String> nodesIterator) {
        StringBuilder pathFromJarToClassBuilder = new StringBuilder();
        while (nodesIterator.hasNext()){
            pathFromJarToClassBuilder.append(nodesIterator.next());
            if (nodesIterator.hasNext()){
                pathFromJarToClassBuilder.append("/");
            }

        }
        return pathFromJarToClassBuilder.toString();
    }


}
