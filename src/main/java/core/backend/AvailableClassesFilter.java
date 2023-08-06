package core.backend;

import core.dto.ApplicatonState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AvailableClassesFilter {

    private ApplicatonState applicatonState;

    public AvailableClassesFilter(ApplicatonState applicatonState) {
        this.applicatonState = applicatonState;
    }

    public Set<String> getClassesStartingWith (String prefix){
        List<String> availableClassNames = applicatonState.getAvailableClassNames();
        List<String> classNamesCopy = new ArrayList<>(availableClassNames);
        return classNamesCopy.stream().filter(className->className.startsWith(prefix)).collect(Collectors.toSet());
    }

}
