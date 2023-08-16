package root.core.jdk.manipulating;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.stereotype.Component;
import root.core.codecompletion.ClassNamesCollector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class ClassesFromJarsExtractor {

    private ClassNamesCollector classNamesCollector;

    public ClassesFromJarsExtractor(ClassNamesCollector classNamesCollector) {
        this.classNamesCollector = classNamesCollector;
    }

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
                        String fileName = zipEntry.getName();
                        if (fileName.endsWith(".class") && !fileName.contains("$")){
                            JavaClass parsedClass = new ClassParser(zipFile.getName(), fileName).parse();
                            classNamesCollector.addClassIfAccessible(parsedClass, pathToJar);
                            File e = new File(fileName);
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
