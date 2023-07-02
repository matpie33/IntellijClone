package core.uievents;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Set;

@Component
public class UIEventsQueue {

    private Set<UIEventHandler> handlers;

    public UIEventsQueue(Set<UIEventHandler> handlers) {
        this.handlers = handlers;
    }

    public void handleEvent(UIEventType eventType, JComponent... data){
        handlers.stream().filter(handler -> handler.handledEventTypes().contains(eventType))
                .forEach(handler->handler.handleEvent(eventType, data));

    }

}
