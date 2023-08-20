package root.core.keylisteners;

import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@Component
public class CreateJavaClassKeyListener extends KeyAdapter {

    private UIEventsQueue uiEventsQueue;

    private JList<String> list;

    public CreateJavaClassKeyListener(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }


    public void setList(JList<String> list) {
        this.list = list;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            uiEventsQueue.dispatchEvent(UIEventType.JAVA_CLASS_NAME_CHOSEN, new Object());
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex<list.getModel().getSize()-1){
                list.setSelectedIndex(selectedIndex +1);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex>0){
                list.setSelectedIndex(selectedIndex -1);
            }
        }
    }


}


