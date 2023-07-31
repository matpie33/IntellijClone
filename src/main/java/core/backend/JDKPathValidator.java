package core.backend;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JDKPathValidator {

    private List<String> directoriesRequired = Arrays.asList("bin", "conf", "include", "jmods", "legal", "lib");

    public boolean isPathValid (File directory){

        File[] files = directory.listFiles();
        List<String> directoriesLocalVar = new ArrayList<>(directoriesRequired);
        if (files == null){
            return false;
        }
        for (File file : files) {
            directoriesLocalVar.remove(file.getName());
        }

        return directoriesLocalVar.isEmpty();
    }

}
