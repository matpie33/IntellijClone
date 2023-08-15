package root.core.keylisteners;

import org.springframework.stereotype.Component;
import root.core.codecompletion.AvailableClassesFilter;
import root.core.dto.ClassNavigationDTO;
import root.core.dto.ClassSuggestionDTO;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.text.JTextComponent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class ClassSearchKeyListener extends KeyAdapter {

    private AvailableClassesFilter availableClassesFilter;

    private UIEventsQueue uiEventsQueue;


    public ClassSearchKeyListener(AvailableClassesFilter availableClassesFilter, UIEventsQueue uiEventsQueue) {
        this.availableClassesFilter = availableClassesFilter;
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar()==KeyEvent.VK_ENTER){
            uiEventsQueue.dispatchEvent(UIEventType.CLASS_CHOSEN_TO_OPEN, new Object());
        }
        else{
            JTextComponent source = (JTextComponent) e.getSource();
            String prefix = source.getText();
            char typedCharacter = e.getKeyChar();
            if (Character.isLetterOrDigit(typedCharacter)){
                prefix += typedCharacter;
            }
            Map<String, Collection<ClassNavigationDTO>> filteredClasses = availableClassesFilter.getClassesStartingWith(prefix);
            List<ClassSuggestionDTO> suggestedClasses = new ArrayList<>();
            for (Map.Entry<String, Collection<ClassNavigationDTO>> nameToPackagesMapping : filteredClasses.entrySet()) {
                Collection<ClassNavigationDTO> classNavigationDTOS = nameToPackagesMapping.getValue();
                for (ClassNavigationDTO classNavigationDTO : classNavigationDTOS){
                    suggestedClasses.add(new ClassSuggestionDTO(classNavigationDTO.getRootDirectory(), nameToPackagesMapping.getKey(), classNavigationDTO.getPackageName(), classNavigationDTO.getOrigin()));
                }
            }
            uiEventsQueue.dispatchEvent(UIEventType.CLASS_NAMES_FILTERED, suggestedClasses);
        }
    }
}
