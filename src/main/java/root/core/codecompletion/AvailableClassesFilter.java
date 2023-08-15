package root.core.codecompletion;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ClassNavigationDTO;

import java.util.*;

@Component
public class AvailableClassesFilter {

    private ApplicationState applicationState;

    public AvailableClassesFilter(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public Map<String, Collection<ClassNavigationDTO>> getClassesStartingWith (String prefix){
        Collection<ClassNavigationDTO> availableClassNames = applicationState.getAvailableClassNames();
        List<ClassNavigationDTO> classNamesCopy = new ArrayList<>(availableClassNames);
        Map<String, Collection<ClassNavigationDTO>> classToPackageNamesMap = new TreeMap<>();
        if (prefix.isEmpty()){
            return new HashMap<>();
        }
        for (ClassNavigationDTO classNameDTO : classNamesCopy) {
            String className = classNameDTO.getClassName();
            if (className.startsWith(prefix)){
                Collection<ClassNavigationDTO> packageNames = applicationState.getPackageNamesForClass(className);
                classToPackageNamesMap.put(className, packageNames);
            }
        }
        return classToPackageNamesMap;
    }

}
