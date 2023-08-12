package root.core.uievents;

public interface UIEventObserver {

    void handleEvent(UIEventType eventType, Object data);


}
