package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.dto.CreateClassDTO;
import root.core.dto.FileSystemChangeDTO;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.ui.tree.ProjectStructureNode;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.ui.components.CreateClassPopup;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CreateJavaClassListener extends ContextAction<ProjectStructureSelectionContextDTO> implements UIEventObserver {


    private final CreateClassPopup createClassPopup;
    private ProjectStructureSelectionContextDTO context;

    private UIEventsQueue uiEventsQueue;

    public CreateJavaClassListener(UIEventsQueue uiEventsQueue, CreateClassPopup createClassPopup) {
        this.uiEventsQueue = uiEventsQueue;
        this.createClassPopup = createClassPopup;
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        createClassPopup.doShow();
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case JAVA_CLASS_NAME_CHOSEN:
                try {
                    if (context.getSelectedPaths().length!=1){
                        throw new RuntimeException("Should not happen");
                    }
                    TreePath selectedPath = context.getSelectedPaths()[0];
                    List<String> nodes = getNodePath(selectedPath);
                    if (!new HashSet<>(nodes).containsAll(Arrays.asList("src", "main", "java"))){
                        return;
                    }
                    removeNodesBeforeSrcMainJava(nodes);
                    String packageName = String.join(".", nodes);
                    CreateClassDTO createClassDTO = createClassPopup.getData();
                    String templateInString = getFileTemplate(createClassDTO);

                    String className = createClassDTO.getClassName();
                    String createdFileContents = String.format(templateInString, packageName, className);

                    File selectedFile = context.getSelectedFile();
                    File file = selectedFile.toPath().resolve(className +".java").toFile();
                    boolean isCreated = file.createNewFile();
                    if (!isCreated){
                        throw new RuntimeException();
                    }
                    Files.write(file.toPath(), createdFileContents.getBytes());
                    FileSystemChangeDTO fileSystemChangeDTO = new FileSystemChangeDTO(List.of(file.toPath()), new ArrayList<>(), new ArrayList<>());
                    uiEventsQueue.dispatchEvent(UIEventType.PROJECT_STRUCTURE_CHANGED, fileSystemChangeDTO);
                    createClassPopup.clear();
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
        }
    }

    private String getFileTemplate(CreateClassDTO createClassDTO) throws IOException, URISyntaxException {
        String templateFile;
        switch (createClassDTO.getClassType()) {
            case CLASS:
                templateFile = "/templates/ClassTemplate.txt";
                break;
            case ENUM:
                templateFile = "/templates/EnumTemplate.txt";
                break;
            case INTERFACE:
                templateFile = "/templates/InterfaceTemplate.txt";
                break;
            case ANNOTATION_TYPE:
                templateFile = "/templates/AnnotationTemplate.txt";
                break;
            default:throw new RuntimeException();
        }
        URL resource = getClass().getResource(templateFile);
        List<String> templateContent = Files.readAllLines(Paths.get(resource.toURI()));
        String templateInString = String.join("\n", templateContent);
        return templateInString;
    }

    private void removeNodesBeforeSrcMainJava(List<String> nodes) {
        Iterator<String> iterator = nodes.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            if (!next.equals("java")){
                iterator.remove();
            }
            else{
                iterator.remove();
                break;
            }
        }
    }

    private List<String> getNodePath(TreePath selectedPath) {
        return Arrays.stream(selectedPath.getPath()).map(node -> {
            ProjectStructureNode projectNode = (ProjectStructureNode) node;
            return projectNode.getDisplayName();
        }).collect(Collectors.toList());
    }
}
