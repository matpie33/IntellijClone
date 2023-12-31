package root.core.uievents;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UIEventsQueue {

    private Set<UIEventObserver> observers = new HashSet<>();

    public void dispatchEvent(UIEventType eventType, Object data){
        observers.stream().forEach(handler->handler.handleEvent(eventType, data));
    }

    public void addObserver(UIEventObserver uiEventObserver) {
        observers.add(uiEventObserver);
    }
}
