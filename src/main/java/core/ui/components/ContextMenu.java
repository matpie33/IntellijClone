package core.ui.components;

import javax.swing.*;
import java.util.List;

public class ContextMenu extends JPopupMenu {


    public ContextMenu(List<String> menus) {
        for (String menu : menus) {
            if (menu.equals("-")){
                add (new JSeparator());
            }
            else{

                add(new JMenuItem(menu));
            }

        }
    }
}
