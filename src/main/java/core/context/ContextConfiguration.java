package core.context;

import core.context.providers.ContextProvider;
import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.dto.MenuItemDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ContextConfiguration {

    private ContextMenuValues contextMenuValues;

    private Map<ContextType, ContextProvider<?>> contextProviders = new HashMap<>();

    public ContextConfiguration(ContextMenuValues contextMenuValues, Set<ContextProvider<?>> contextProviders) {
        this.contextMenuValues = contextMenuValues;
        mapContextProviders(contextProviders);
    }

    private void mapContextProviders(Set<ContextProvider<?>> contextProviders) {
        for (ContextProvider<?> contextProvider : contextProviders) {
            this.contextProviders.put(contextProvider.getContextType(), contextProvider);
        }
    }

    public ContextProvider<?> getContextProvider(ContextType contextType) {
        return contextProviders.get(contextType);
    }

    public List<MenuItemDTO> getContextMenuValues(ContextType contextType){
        return contextMenuValues.getValues(contextType);
    }
}
