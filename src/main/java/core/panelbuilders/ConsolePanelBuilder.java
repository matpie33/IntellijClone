package core.panelbuilders;

import core.context.ContextConfiguration;
import core.contextMenu.ContextMenuValues;
import core.contextMenu.ContextType;
import core.mouselisteners.PopupMenuRequestListener;
import core.mouselisteners.TreeNodeDoubleClickListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class ConsolePanelBuilder  {



    private ContextConfiguration contextConfiguration;
    private JPanel panel;

    public ConsolePanelBuilder(ContextConfiguration contextConfiguration) {
        this.contextConfiguration = contextConfiguration;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        JTextArea consoleLabel = new JTextArea("console goes here");
        panel.add(consoleLabel, BorderLayout.CENTER);
        consoleLabel.addMouseListener(new PopupMenuRequestListener(ContextType.CONSOLE, contextConfiguration));
    }

    public JPanel getPanel() {
        return panel;
    }

}
