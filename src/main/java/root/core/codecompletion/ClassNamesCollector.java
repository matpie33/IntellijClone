package root.core.codecompletion;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;

@Component
public class ClassNamesCollector {

    private ApplicationState applicationState;

    public ClassNamesCollector(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public void addClassIfAccessible(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, String packageName){
        if (classOrInterfaceDeclaration.isPublic()){
            applicationState.addClassWithPackage(classOrInterfaceDeclaration.getNameAsString(), packageName);
        }
    }

    public void addClassIfAccessible(JavaClass parsedClass) {
        if (parsedClass.isPublic()){
            applicationState.addClassWithPackage(parsedClass.getClassName());
        }
    }
}
