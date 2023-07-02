package core.menuitemlisteners;

import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;

@Component
public class DefaultMenuItemListener implements MenuItemListener {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("menu click");
    }
}
