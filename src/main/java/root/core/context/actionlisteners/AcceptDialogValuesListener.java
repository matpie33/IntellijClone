package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class AcceptDialogValuesListener extends AbstractAction {

    private UIEventsQueue uiEventsQueue;

    public AcceptDialogValuesListener(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        uiEventsQueue.dispatchEvent(UIEventType.DIALOG_ACCEPT_REQUEST, new Object());
    }
}
