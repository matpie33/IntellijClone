package root.core.codecompletion;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ClassNavigationDTO;

import java.util.*;

@Component
public class AvailableClassesFilter {

    public static final int MAXIMUM_SUGGESTIONS = 100;
    private ApplicationState applicationState;

    private ClassNameMatcher classNameMatcher;

    public AvailableClassesFilter(ApplicationState applicationState, ClassNameMatcher classNameMatcher) {
        this.applicationState = applicationState;
        this.classNameMatcher = classNameMatcher;
    }

    public Map<String, Collection<ClassNavigationDTO>> getClassesStartingWith (String prefix){
        List<ClassNavigationDTO> classNamesCopy = null;
        synchronized ( applicationState.getClassNamesLock()){
            Collection<ClassNavigationDTO> availableClassNames = applicationState.getAvailableClassNames();
            classNamesCopy = new ArrayList<>(availableClassNames);
        }

        Map<String, Collection<ClassNavigationDTO>> classToPackageNamesMap = new TreeMap<>();
        if (prefix.isEmpty()){
            return new HashMap<>();
        }
        Map<ClassMatchType, List<String>> classMatchTypeToClassNameMap = new HashMap<>();
        classMatchTypeToClassNameMap.put(ClassMatchType.PARTIAL_MATCH, new ArrayList<>());
        classMatchTypeToClassNameMap.put(ClassMatchType.FULL_MATCH, new ArrayList<>());
        boolean isAnyFullMatch = false;
        int addedElementsSize = 0;
        for (ClassNavigationDTO classNameDTO : classNamesCopy) {
            String className = classNameDTO.getClassName();
            ClassMatchType classMatchType = classNameMatcher.doesClassNameMatch(className, prefix);
            if (classMatchType.equals(ClassMatchType.FULL_MATCH)){
                isAnyFullMatch = true;
                classMatchTypeToClassNameMap.get(classMatchType).add(className);
            }
            if (!isAnyFullMatch && classMatchType.equals(ClassMatchType.PARTIAL_MATCH)){
                classMatchTypeToClassNameMap.get(classMatchType).add(className);
            }
        }
        if (isAnyFullMatch){
            addMatchedClasses(classMatchTypeToClassNameMap, ClassMatchType.FULL_MATCH, classToPackageNamesMap, addedElementsSize);
        }
        else{
            addMatchedClasses(classMatchTypeToClassNameMap, ClassMatchType.PARTIAL_MATCH, classToPackageNamesMap, addedElementsSize);
        }
        return classToPackageNamesMap;
    }

    private void addMatchedClasses(Map<ClassMatchType, List<String>> classMatchTypeToClassNameMap, ClassMatchType matchType, Map<String, Collection<ClassNavigationDTO>> classToPackageNamesMap, int addedElementsSize) {
        List<String> fullMatched = classMatchTypeToClassNameMap.get(matchType);
        for (String className : fullMatched) {
            Collection<ClassNavigationDTO> packageNames = applicationState.getPackageNamesForClass(className);
            classToPackageNamesMap.put(className, packageNames);
            addedElementsSize++;
            if (addedElementsSize>MAXIMUM_SUGGESTIONS){
                return;
            }
        }
    }

}
