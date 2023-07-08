package core.context.actionlisteners;

import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

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
