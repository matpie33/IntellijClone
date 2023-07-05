package core.uievents;

import java.util.Set;

public interface UIEventObserver {

    void handleEvent(UIEventType eventType, Object data);


}
