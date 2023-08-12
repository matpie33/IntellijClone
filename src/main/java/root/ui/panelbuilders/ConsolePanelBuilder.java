package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.core.constants.FontsConstants;
import root.core.context.ContextConfiguration;
import root.core.context.contextMenu.ContextType;
import root.core.dto.ErrorDTO;
import root.core.mouselisteners.PopupMenuRequestListener;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;

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
        consoleOutput.setFont(consoleOutput.getFont().deriveFont(FontsConstants.FONT_SIZE));
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
