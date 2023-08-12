package root.core.utility;

import org.springframework.stereotype.Component;
import root.core.jdk.manipulating.JDKConfigurationHolder;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventsQueue;

import java.io.IOException;
import java.util.Set;

@Component
public class UIObserversInitializer {

    public UIObserversInitializer(UIEventsQueue uiEventsQueue, Set<UIEventObserver> uiEventObservers, JDKConfigurationHolder JDKConfigurationHolder) {
        uiEventObservers.forEach(uiEventsQueue::addObserver);
        try {
            JDKConfigurationHolder.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
