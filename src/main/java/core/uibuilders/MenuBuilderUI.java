package core.uibuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import core.MenuItemListenersCollection;
import core.menuitemlisteners.MenuItemListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.util.Iterator;

@Component
public class MenuBuilderUI {

    private MenuItemListenersCollection menuItemListenersCollection;

    public MenuBuilderUI( MenuItemListenersCollection menuItemListenersCollection) {
        this.menuItemListenersCollection = menuItemListenersCollection;
    }

    public JMenuBar createMenu() throws IOException {
        JMenuBar menuBar = new JMenuBar();


        XmlMapper xmlMapper = new XmlMapper();
        ObjectNode jsonNode = (ObjectNode) xmlMapper.readTree(getClass().getResource("/menu.xml"));
        Iterator<String> menuIterator = jsonNode.fieldNames();
        while (menuIterator.hasNext()) {
            String menuText = menuIterator.next();
            JMenu menu = new JMenu(menuText);
            menuBar.add(menu);
            JsonNode node = jsonNode.get(menuText);
            Iterator<String> menuItems = node.fieldNames();
            while (menuItems.hasNext()) {
                String menuItem = menuItems.next();
                if (node.get(menuItem).has("newGroup")) {
                    menu.add(new JSeparator());
                }
                JMenuItem menuItem1 = new JMenuItem(menuItem.replaceAll("_", " "));
                MenuItemListener menuItemListener =menuItemListenersCollection.getItemListenerByName(menuItem);
                menuItem1.addActionListener(menuItemListener);
                menu.add(menuItem1);
            }
        }
        return menuBar;
    }
}