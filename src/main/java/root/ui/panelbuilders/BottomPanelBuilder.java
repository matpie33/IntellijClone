package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.core.constants.FontsConstants;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class BottomPanelBuilder implements UIEventObserver {

    private JPanel mainPanel;
    private JLabel messageLabel;


    @PostConstruct
    public void init (){
        createMainPanel();

    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(messageLabel.getFont().deriveFont((float) FontsConstants.FONT_SIZE));
        mainPanel.add(messageLabel);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case APPLICATION_MESSAGE_ARRIVED:
                messageLabel.setText((String) data);
                break;
        }
    }
}
