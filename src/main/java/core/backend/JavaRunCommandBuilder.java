package core.backend;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Name;
import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaRunCommandBuilder {

    private ApplicatonState applicatonState;

    public JavaRunCommandBuilder(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public String[] build (File mainClass){
        try {
            String pathToMainClass = getFullPathToMainClass(mainClass);
            String classPath = applicatonState.getClassPath();
            List<String> commands = new ArrayList<>();
            commands.add("java");
            commands.add("-classpath");
            commands.add(classPath);
            commands.add( pathToMainClass);
            return commands.toArray(new String[]{});
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private String getFullPathToMainClass(File mainClass) throws FileNotFoundException {
        CompilationUnit result = StaticJavaParser.parse(mainClass);
        ClassOrInterfaceDeclaration classType = (ClassOrInterfaceDeclaration) result.getType(0);
        String className = classType.getFullyQualifiedName().orElseThrow();
        className = className.replace(".", "/");
        return className;
    }

}
