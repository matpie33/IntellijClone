package core;

import core.dto.DirectoryDTO;
import core.dto.FileDTO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectStructureReader {

    public List<FileDTO> readProjectDirectory (File file){
        List<FileDTO> files = getFilesStructure(file);
        return files;

    }

    private List<FileDTO> getFilesStructure(File file) {
        File[] files = file.listFiles();
        List<FileDTO> filesTree = new ArrayList<>();
        for (File fileInDirectory : files) {
            if (fileInDirectory.isDirectory()){
                DirectoryDTO directory = new DirectoryDTO(fileInDirectory.getName(), fileInDirectory.getAbsolutePath());
                filesTree.add(directory);
                directory.setFiles(getFilesStructure(fileInDirectory));
            }
            else{
               filesTree.add( new FileDTO(fileInDirectory.getName(), fileInDirectory.getAbsolutePath()));
            }
        }
        return filesTree;
    }

}
