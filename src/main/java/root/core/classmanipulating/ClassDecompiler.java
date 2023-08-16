package root.core.classmanipulating;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.springframework.stereotype.Component;
import root.core.dto.FileReadResultDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class ClassDecompiler {

    public static final String JAR_EXTENSION = ".jar";

    public FileReadResultDTO decompile (Path pathToFile){
        try {

            String path = pathToFile.toString();
            int indexOfPathAfterJar = path.indexOf(JAR_EXTENSION) + JAR_EXTENSION.length();
            String pathToJarFile = path.substring(0, indexOfPathAfterJar);
            String pathFromJarToClass = path.substring(indexOfPathAfterJar+1);
            String nameOfClassToBeDecompiled = pathFromJarToClass.replace(".class", "").replace("\\", "/");
            List<File> classNames = new ArrayList<>();
            try (ZipFile zipFile = new ZipFile(pathToJarFile)) {
                Enumeration<? extends ZipEntry> filesInZip = zipFile.entries();
                String decompilationResultFile = "";
                while (filesInZip.hasMoreElements()){
                    ZipEntry fileInZip = filesInZip.nextElement();
                    String fileName = fileInZip.getName().replace(".class", "");
                    if (fileName.startsWith(nameOfClassToBeDecompiled)){
                        InputStream fileContentsStream = zipFile.getInputStream(fileInZip);
                        File fileWithClassContents = File.createTempFile("File", ".class");
                        Files.write(fileWithClassContents.toPath(), fileContentsStream.readAllBytes());
                        if (fileName.equals(nameOfClassToBeDecompiled)){
                            decompilationResultFile = fileWithClassContents.getAbsolutePath();
                        }
                        classNames.add(fileWithClassContents);
                    }
                }
                List<String> decompilationResult = decompile(classNames, decompilationResultFile);
                return createResultDTO(pathToJarFile, pathFromJarToClass, decompilationResult);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private FileReadResultDTO createResultDTO(String pathToJarFile, String pathFromJarToClass, List<String> decompilationResult) {
        FileReadResultDTO fileReadResultDTO = new FileReadResultDTO();
        fileReadResultDTO.setClassOrigin(ClassOrigin.MAVEN);
        fileReadResultDTO.setReadSuccessfully(true);
        Path pathToFile = Path.of(pathToJarFile, pathFromJarToClass);
        String pathFromRoot = pathToFile.toString();
        fileReadResultDTO.setFile(pathToFile.toFile());
        fileReadResultDTO.setPathFromRoot(pathFromRoot);
        fileReadResultDTO.setContentLines(decompilationResult);
        return fileReadResultDTO;
    }

    private List<String> decompile(List<File> fileForClassContents, String decompilationResultFile) throws IOException {
        File tempFileDirectory = fileForClassContents.get(0).getParentFile();
        ConsoleDecompiler consoleDecompiler = new ConsoleDecompiler(tempFileDirectory, new HashMap<>());
        fileForClassContents.forEach(file->consoleDecompiler.addSpace(file, true));
        consoleDecompiler.decompileContext();
        File fileWithDecompilationResult = new File(decompilationResultFile.replace(".class", ".java"));
        return Files.readAllLines(fileWithDecompilationResult.toPath());
    }



}
