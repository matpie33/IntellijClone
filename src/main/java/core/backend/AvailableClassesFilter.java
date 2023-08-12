package core.backend;

import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AvailableClassesFilter {

    private ApplicatonState applicatonState;

    public AvailableClassesFilter(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public Map<String, Collection<String>> getClassesStartingWith (String prefix){
        Deque<String> availableClassNames = applicatonState.getAvailableClassNames();
        List<String> classNamesCopy = new ArrayList<>(availableClassNames);
        Map<String, Collection<String>> classToPackageNamesMap = new TreeMap<>();
        if (prefix.isEmpty()){
            return new HashMap<>();
        }
        for (String className : classNamesCopy) {
            if (className.startsWith(prefix)){
                classToPackageNamesMap.put(className, applicatonState.getPackageNamesForClass(className));
            }
        }
        return classToPackageNamesMap;
    }

}
