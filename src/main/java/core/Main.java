package core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false);
        builder.run(args);

    }

    public Main () throws IOException {
        JFrame frame = new JFrame();
        JMenuBar menu = createMenu();
        frame.setJMenuBar(menu);
        frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        frame.setVisible(true);

    }

    private JMenuBar createMenu() throws IOException {
        JMenuBar menuBar = new JMenuBar();


        XmlMapper xmlMapper = new XmlMapper();
        ObjectNode jsonNode = (ObjectNode) xmlMapper.readTree(getClass().getResource("/menu.xml"));
        Iterator<String> menuIterator = jsonNode.fieldNames();
        while (menuIterator.hasNext()){
            String menuText = menuIterator.next();
            JMenu menu = new JMenu(menuText);
            menuBar.add(menu);
            JsonNode node = jsonNode.get(menuText);
            Iterator<String> menuItems = node.fieldNames();
            while (menuItems.hasNext()){
                String menuItem = menuItems.next();
                if (node.get(menuItem).has("newGroup")){
                    menu.add(new JSeparator());
                }
                menu.add(new JMenuItem(menuItem.replaceAll("_", " ")));
            }
        }
        return menuBar;
    }

}
