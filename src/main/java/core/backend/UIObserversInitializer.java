package core.backend;

import core.uievents.UIEventObserver;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class UIObserversInitializer {

    public UIObserversInitializer(UIEventsQueue uiEventsQueue, Set<UIEventObserver> uiEventObservers, ConfigurationHolder configurationHolder) {
        uiEventObservers.forEach(uiEventsQueue::addObserver);
        try {
            configurationHolder.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
