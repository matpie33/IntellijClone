package core.context.actionlisteners;

import core.contextMenu.ContextType;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
public class EmptyActionListener implements ContextActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("click");
    }

    @Override
    public void setContext(Object context) {

    }

}
