package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class CloseDialogListener extends AbstractAction {

    private UIEventsQueue uiEventsQueue;

    public CloseDialogListener(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        uiEventsQueue.dispatchEvent(UIEventType.DIALOG_CLOSE_REQUEST, new Object());
    }
}
