package root.core.keylisteners;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ListNavigationKeyListener extends KeyAdapter {

    private final JList list;

    public ListNavigationKeyListener (JList list){
        this.list = list;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            list.setSelectedIndex(list.getSelectedIndex()+1);
        }
        if (e.getKeyCode()==KeyEvent.VK_UP){
            list.setSelectedIndex(list.getSelectedIndex()-1);
        }
        list.ensureIndexIsVisible(list.getSelectedIndex());
        super.keyPressed(e);
    }
}

