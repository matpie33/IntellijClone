package core.contextMenu;

import core.context.actionlisteners.ContextAction;
import core.context.actionlisteners.EmptyActionListener;
import core.context.actionlisteners.FileDeleteRequestListener;
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
        menuItemsMap.put(ContextType.PROJECT_STRUCTURE, Arrays.asList(new MenuItemDTO("New", getBean(EmptyActionListener.class)), new MenuItemDTO("-", getBean(EmptyActionListener.class)), new MenuItemDTO("New", getBean(EmptyActionListener.class)),
                new MenuItemDTO("Copy", getBean(EmptyActionListener.class)),
                new MenuItemDTO("Delete", getBean(FileDeleteRequestListener.class))));
        menuItemsMap.put(ContextType.FILE_EDITOR, Arrays.asList(new MenuItemDTO("Extract method", getBean(EmptyActionListener.class)),
                new MenuItemDTO("go to", getBean(EmptyActionListener.class)),
                new MenuItemDTO("Find usages", getBean(EmptyActionListener.class))));
        menuItemsMap.put(ContextType.CONSOLE, Arrays.asList(new MenuItemDTO("Pause output", getBean(EmptyActionListener.class)), new MenuItemDTO("Fold lines", getBean(EmptyActionListener.class))));
        menuItemsMap.put(ContextType.CLASS_STRUCTURE, Arrays.asList(new MenuItemDTO("Open in", getBean(EmptyActionListener.class)),
                new MenuItemDTO("Find usages", getBean(EmptyActionListener.class)), new MenuItemDTO("Jump to source", getBean(EmptyActionListener.class))));
    }

    public List<MenuItemDTO> getValues (ContextType contextType){
        return menuItemsMap.get(contextType);
    }

    private ContextAction getBean(Class<? extends ContextAction> classType){
        return applicationContext.getBean(classType);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
