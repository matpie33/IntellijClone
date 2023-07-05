package core.backend;

import core.uievents.UIEventObserver;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UIObserversInitializer {

    public UIObserversInitializer(UIEventsQueue uiEventsQueue, Set<UIEventObserver> uiEventObservers) {
        uiEventObservers.forEach(uiEventsQueue::addObserver);
    }
}
