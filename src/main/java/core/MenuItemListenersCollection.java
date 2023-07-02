package core;

import core.menuitemlisteners.DefaultMenuItemListener;
import core.menuitemlisteners.MenuItemListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MenuItemListenersCollection {

    private Set<MenuItemListener> actionListeners;

    private DefaultMenuItemListener defaultMenuItemListener;

    public MenuItemListenersCollection(Set<MenuItemListener> actionListeners, DefaultMenuItemListener defaultMenuItemListener) {
        this.actionListeners = actionListeners;
        this.defaultMenuItemListener = defaultMenuItemListener;
    }

    public MenuItemListener getItemListenerByName (String name){
        return actionListeners.stream().filter(m->m.getName().equals(name)).findFirst().orElse(defaultMenuItemListener);
    }

}
