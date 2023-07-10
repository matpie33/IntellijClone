package core.panelbuilders;

import core.context.ContextConfiguration;
import core.contextMenu.ContextType;
import core.dto.ErrorDTO;
import core.mouselisteners.PopupMenuRequestListener;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

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
                writeLineToConsole((String) data);
                JScrollBar verticalScrollBar = outputScrollPane.getVerticalScrollBar();
                SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
                break;
            case ERROR_OCCURRED:
                ErrorDTO errorDTO = (ErrorDTO) data;
                Throwable exception = errorDTO.getException();
                writeLineToConsole(exception.getMessage());
                for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
                    writeLineToConsole(stackTraceElement.toString());
                }
        }
    }

    private void writeLineToConsole(String data) {
        consoleOutput.append(data);
        consoleOutput.append("\n");
    }
}
