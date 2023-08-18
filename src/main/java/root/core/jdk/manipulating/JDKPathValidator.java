package root.core.jdk.manipulating;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JDKPathValidator {

    private List<String> directoriesRequired = Arrays.asList("bin", "conf", "include", "jmods", "legal", "lib");

    private boolean isValid;

    public void validate(File directory){

        File[] files = directory.listFiles();
        List<String> directoriesLocalVar = new ArrayList<>(directoriesRequired);
        if (files == null){
            isValid = false;
        }
        if (files != null){
            for (File file : files) {
                directoriesLocalVar.remove(file.getName());
            }
        }

        isValid =  directoriesLocalVar.isEmpty();
    }

    public boolean isValid() {
        return isValid;
    }
}
