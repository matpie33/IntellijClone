package root.core.menuitemlisteners;

import org.springframework.stereotype.Component;
import root.ui.panelbuilders.ClassSearchPanelBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class NavigateToClassMenuItemListener extends AbstractAction implements MenuItemListener {


    private ClassSearchPanelBuilder classSearchPanelBuilder;

    public NavigateToClassMenuItemListener(ClassSearchPanelBuilder classSearchPanelBuilder) {
        this.classSearchPanelBuilder = classSearchPanelBuilder;
    }

    @Override
    public String getName() {
        return "Class";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        classSearchPanelBuilder.showPopup();

    }

}
