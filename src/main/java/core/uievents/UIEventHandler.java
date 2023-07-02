package core.uievents;

import javax.swing.*;
import java.util.Set;

public interface UIEventHandler {
    void handleEvent (UIEventType eventType, JComponent... data);

    Set<UIEventType> handledEventTypes();

}
