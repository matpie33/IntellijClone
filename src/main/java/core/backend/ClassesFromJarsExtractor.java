package core.backend;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class ClassesFromJarsExtractor {

    public Map<String, List<File>> extractClassesFromJars (String classpath){
        Map<String, List<File>> classFilesPerJar = new HashMap<>();
        for (String pathToJar : classpath.split(";")) {
            try {
                if (!pathToJar.endsWith(".jar")){
                    continue;
                }
                try (ZipFile zipFile = new ZipFile(pathToJar)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    List<File> classes = new ArrayList<>();
                    classFilesPerJar.put(pathToJar, classes);
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().endsWith(".class")){
                            File e = new File(zipEntry.getName());
                            classes.add(e);
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
