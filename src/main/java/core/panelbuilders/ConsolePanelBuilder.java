package core.panelbuilders;

import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.mouselisteners.PopupMenuRequestListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.io.File;

@Component
public class ConsolePanelBuilder implements UIEventObserver {



    private ContextConfiguration contextConfiguration;
    private JPanel panel;
    private JTextArea consoleOutput;
    private JScrollPane outputScrollPane;

    public ConsolePanelBuilder(ContextConfiguration contextConfiguration) {
        this.contextConfiguration = contextConfiguration;
    }

    @PostConstruct
    public void init (){
        panel = new JPanel(new BorderLayout());
        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        outputScrollPane = new JScrollPane(consoleOutput);
        panel.add(outputScrollPane, BorderLayout.CENTER);
        consoleOutput.addMouseListener(new PopupMenuRequestListener(ContextType.CONSOLE, contextConfiguration));
    }

    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType) {
            case CONSOLE_DATA_AVAILABLE:
                consoleOutput.append((String) data);
                consoleOutput.append("\n");
                JScrollBar verticalScrollBar = outputScrollPane.getVerticalScrollBar();
                SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
                break;
        }
    }
}
