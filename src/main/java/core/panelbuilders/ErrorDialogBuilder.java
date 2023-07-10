package core.panelbuilders;

import core.Main;
import core.dto.ErrorDTO;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class ErrorDialogBuilder implements UIEventObserver {
    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case ERROR_OCCURRED:
                ErrorDTO errorDTO = (ErrorDTO) data;
                JOptionPane.showMessageDialog(Main.FRAME, errorDTO.getMessage());
        }
    }
}
