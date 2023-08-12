package root.core.codecompletion;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;

import java.util.*;

@Component
public class AvailableClassesFilter {

    private ApplicationState applicationState;

    public AvailableClassesFilter(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public Map<String, Collection<String>> getClassesStartingWith (String prefix){
        Deque<String> availableClassNames = applicationState.getAvailableClassNames();
        List<String> classNamesCopy = new ArrayList<>(availableClassNames);
        Map<String, Collection<String>> classToPackageNamesMap = new TreeMap<>();
        if (prefix.isEmpty()){
            return new HashMap<>();
        }
        for (String className : classNamesCopy) {
            if (className.startsWith(prefix)){
                classToPackageNamesMap.put(className, applicationState.getPackageNamesForClass(className));
            }
        }
        return classToPackageNamesMap;
    }

}
