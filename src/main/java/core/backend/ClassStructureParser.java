package core.backend;

import com.github.javaparser.ParseProblemException;
import core.dto.ApplicatonState;
import core.dto.ClassStructureDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;

@Component
public class ClassStructureParser {

    private ApplicatonState applicatonState;

    private ClassStructureVisitor classStructureVisitor;

    public ClassStructureParser(ApplicatonState applicatonState, ClassStructureVisitor classStructureVisitor) {
        this.applicatonState = applicatonState;
        this.classStructureVisitor = classStructureVisitor;
    }

    public boolean parseClassStructure(File file){
        try {
            classStructureVisitor.visitFile(file);
            ClassStructureDTO classStructure = classStructureVisitor.getClassStructure();
            applicatonState.putClassStructure(file, classStructure);
            return classStructureVisitor.hasMainMethod();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (ParseProblemException ex){
            applicatonState.addClassWithCompilationError(file);
        }
        return false;
    }




}
