package core.contextMenu;

import core.context.actionlisteners.*;
import core.context.conditionalmenu.ConditionChecker;
import core.context.conditionalmenu.MainMethodConditionChecker;
import core.dto.MenuItemDTO;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextMenuValues implements ApplicationContextAware {

    private Map<ContextType, List<MenuItemDTO>> menuItemsMap = new HashMap<> ();
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init (){
        menuItemsMap.put(ContextType.PROJECT_STRUCTURE, Arrays.asList(new MenuItemDTO("New", getContextAction(EmptyActionListener.class)), new MenuItemDTO("-", getContextAction(EmptyActionListener.class)), new MenuItemDTO("New", getContextAction(EmptyActionListener.class)),
                new MenuItemDTO("Rename", getContextAction(FileRenameListener.class)),
                new MenuItemDTO("Run main method", getContextAction(MainMethodRunListener.class), getConditionChecker(MainMethodConditionChecker.class)),
                new MenuItemDTO("Delete", getContextAction(FileDeleteRequestListener.class))));
        menuItemsMap.put(ContextType.FILE_EDITOR, Arrays.asList(new MenuItemDTO("Extract method", getContextAction(EmptyActionListener.class)),
                new MenuItemDTO("go to", getContextAction(EmptyActionListener.class)),
                new MenuItemDTO("Find usages", getContextAction(EmptyActionListener.class))));
        menuItemsMap.put(ContextType.CONSOLE, Arrays.asList(new MenuItemDTO("Pause output", getContextAction(EmptyActionListener.class)), new MenuItemDTO("Fold lines", getContextAction(EmptyActionListener.class))));
        menuItemsMap.put(ContextType.CLASS_STRUCTURE, Arrays.asList(new MenuItemDTO("Open in", getContextAction(EmptyActionListener.class)),
                new MenuItemDTO("Find usages", getContextAction(EmptyActionListener.class)), new MenuItemDTO("Jump to source", getContextAction(EmptyActionListener.class))));
    }

    public List<MenuItemDTO> getValues (ContextType contextType){
        return menuItemsMap.get(contextType);
    }

    private ContextAction<?> getContextAction(Class<? extends ContextAction<?>> classType){
        return applicationContext.getBean(classType);
    }
    private ConditionChecker getConditionChecker(Class<? extends ConditionChecker> classType){
        return applicationContext.getBean(classType);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
