package core.backend;

import core.dto.FileReadResultDTO;
import core.dto.TreeNodeFileDTO;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.springframework.stereotype.Component;

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

    public static final String MAVEN_ROOT_NODE_NAME = "maven";
    public static final String CLASS_FILE_EXTENSION = ".class";

    public FileReadResultDTO decompile (TreeNodeFileDTO[] nodesPath){
        try {

            Iterator<TreeNodeFileDTO> nodesIterator = Arrays.stream(nodesPath).iterator();
            String pathToJarFile = getPathToJarFile(nodesPath, nodesIterator);
            nodesIterator.next(); // ignore node with jar path
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
        fileReadResultDTO.setReaded(true);
        Path pathToFile = Path.of(pathToJarFile, pathFromJarToClass);
        String pathFromRoot = pathToFile.toString();
        fileReadResultDTO.setFile(pathToFile.toFile());
        fileReadResultDTO.setPathFromRoot(pathFromRoot);
        fileReadResultDTO.setLines(decompilationResult);
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

    private String getPathFromJarToClass(Iterator<TreeNodeFileDTO> nodesIterator) {
        StringBuilder pathFromJarToClassBuilder = new StringBuilder();
        while (nodesIterator.hasNext()){
            TreeNodeFileDTO node = nodesIterator.next();
            String directoryName = node.getDisplayName();
            if (!directoryName.endsWith(CLASS_FILE_EXTENSION)){
                directoryName = directoryName.replace('.', '/');
            }
            pathFromJarToClassBuilder.append(directoryName);
            if (nodesIterator.hasNext()){
                pathFromJarToClassBuilder.append("/");
            }

        }
        String pathFromJarToClass = pathFromJarToClassBuilder.toString();
        return pathFromJarToClass;
    }

    private String getPathToJarFile(TreeNodeFileDTO[] nodesPath, Iterator<TreeNodeFileDTO> nodesIterator) {
        while (nodesIterator.hasNext()){
            TreeNodeFileDTO node = nodesIterator.next();
            if (node.getDisplayName().equals(MAVEN_ROOT_NODE_NAME)){
                break;
            }
        }
        String pathToJarFile = nodesPath[nodesPath.length - 1].getJarPath();
        return pathToJarFile;
    }

}
