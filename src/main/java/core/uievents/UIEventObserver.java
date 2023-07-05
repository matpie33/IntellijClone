package core.uievents;

import java.util.Set;

public abstract class UIEventObserver {

    public UIEventObserver (UIEventsQueue uiEventsQueue){
        uiEventsQueue.addObserver(this);
    }

    public abstract void handleEvent(UIEventType eventType, Object data);


}
