package root.core.codecompletion;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ClassNavigationDTO;

import java.util.*;

@Component
public class AvailableClassesFilter {

    public static final int MAXIMUM_SUGGESTIONS = 100;
    private ApplicationState applicationState;

    public AvailableClassesFilter(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public Map<String, Collection<ClassNavigationDTO>> getClassesStartingWith (String prefix){
        Object classNamesLock = applicationState.getClassNamesLock();
        List<ClassNavigationDTO> classNamesCopy = null;
        synchronized ( classNamesLock){
            Collection<ClassNavigationDTO> availableClassNames = applicationState.getAvailableClassNames();
            classNamesCopy = new ArrayList<>(availableClassNames);
        }

        Map<String, Collection<ClassNavigationDTO>> classToPackageNamesMap = new TreeMap<>();
        if (prefix.isEmpty()){
            return new HashMap<>();
        }
        int addedElementsSize = 0;
        for (ClassNavigationDTO classNameDTO : classNamesCopy) {
            String className = classNameDTO.getClassName();
            if (className.startsWith(prefix)){
                Collection<ClassNavigationDTO> packageNames = applicationState.getPackageNamesForClass(className);
                classToPackageNamesMap.put(className, packageNames);
                addedElementsSize++;
            }
            if (addedElementsSize> MAXIMUM_SUGGESTIONS){
                break;
            }
        }
        return classToPackageNamesMap;
    }

}
