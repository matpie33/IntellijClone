package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;

@Component
public class EmptyActionListener extends ContextAction<Object> {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("click");
    }

    @Override
    public void setContext(Object context) {

    }

}
