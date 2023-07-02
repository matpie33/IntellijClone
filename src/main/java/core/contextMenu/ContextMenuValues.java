package core.contextMenu;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextMenuValues {

    private Map<ContextType, List<String>> menuItemsMap = new HashMap<> ();

    @PostConstruct
    public void init (){
        menuItemsMap.put(ContextType.PROJECT_STRUCTURE, Arrays.asList("New", "-", "Cut", "Copy", "Copy path/reference", "-", "Find usages", "Refactor"));
        menuItemsMap.put(ContextType.FILE_EDITOR, Arrays.asList("Find usages", "Go to", "copy"));
        menuItemsMap.put(ContextType.CONSOLE, Arrays.asList("Pause output", "Fold lines"));
        menuItemsMap.put(ContextType.FILE_STRUCTURE, Arrays.asList("Toggle method breakpoint", "Compare with", "Diagrams"));
    }

    public List<String> getValues (ContextType contextType){
        return menuItemsMap.get(contextType);
    }


}
