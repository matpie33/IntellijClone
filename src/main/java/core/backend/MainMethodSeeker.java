package core.backend;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;

@Component
public class MainMethodSeeker {

    public boolean findMainMethod (File file){
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(file);
            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().iterator().next();
            for (MethodDeclaration method : classDeclaration.getMethods()) {
                if (isMethodSignatureMatchingMain(method)){
                    boolean isMainMethod = checkIfMethodHas1ArrayParameterOfTypeString(file, method);
                    if (isMainMethod){
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean isMethodSignatureMatchingMain(MethodDeclaration method) {
        return method.isStatic() && method.isPublic() && method.getType().isVoidType();
    }

    private boolean checkIfMethodHas1ArrayParameterOfTypeString(File file, MethodDeclaration method) {
        NodeList<Parameter> parameters = method.getParameters();
        if (parameters.size() ==1){
            Parameter parameter = parameters.iterator().next();
            if (parameter.getType().isArrayType() && parameter.getType().getElementType().toString().equals("String")){
                return true;
            }
        }
        return false;
    }

}
