package root.core.jdk.manipulating;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.stereotype.Component;
import root.core.codecompletion.ClassNamesCollector;
import root.core.dto.FileDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class ClassesFromJarsExtractor {

    private ClassNamesCollector classNamesCollector;

    public ClassesFromJarsExtractor(ClassNamesCollector classNamesCollector) {
        this.classNamesCollector = classNamesCollector;
    }

    public Map<String, List<FileDTO>> extractClassesFromJars (String classpath){
        Map<String, List<FileDTO>> classFilesPerJar = new HashMap<>();
        for (String pathToJar : classpath.split(";")) {
            try {
                if (!pathToJar.endsWith(".jar")){
                    continue;
                }
                try (ZipFile zipFile = new ZipFile(pathToJar)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    List<FileDTO> classes = new ArrayList<>();
                    classFilesPerJar.put(pathToJar, classes);
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String fileName = zipEntry.getName();
                        boolean isClassFile = fileName.endsWith(".class");
                        boolean isDirectory = zipEntry.isDirectory();
                        if (isDirectory || !fileName.contains("$")){
                            if (isClassFile){
                                JavaClass parsedClass = new ClassParser(zipFile.getName(), fileName).parse();
                                classNamesCollector.addClassIfAccessible(parsedClass, pathToJar);
                            }
                            Path path = Path.of(fileName);
                            classes.add(new FileDTO(path, isDirectory));
                        }

                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }



        return classFilesPerJar;
    }

}
