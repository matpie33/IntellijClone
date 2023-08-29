package root.core.codecompletion;

import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.stereotype.Component;
import root.core.classmanipulating.ClassOrigin;
import root.core.dto.ApplicationState;

@Component
public class ClassNamesCollector {

    private ApplicationState applicationState;

    public ClassNamesCollector(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public void addClassIfAccessible(TypeDeclaration classOrInterfaceDeclaration, String packageName, ClassOrigin origin, String rootDirectory){
        if (classOrInterfaceDeclaration.isPublic()){
            applicationState.addClassWithPackage(classOrInterfaceDeclaration.getNameAsString(), packageName, origin, rootDirectory);
        }
    }

    public void addClassIfAccessible(JavaClass parsedClass, String pathToJar) {
        if (parsedClass.isPublic()){
            applicationState.addClassWithPackage(parsedClass.getClassName(), pathToJar, ClassOrigin.MAVEN);
        }
    }
}
