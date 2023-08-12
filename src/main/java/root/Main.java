package root;

import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import root.core.directory.changesdetecting.DirectoryChangesDetector;
import root.core.dto.ApplicationState;
import root.core.nodehandling.ProjectStructureNodesHandler;
import root.core.shortcuts.ApplicationShortcuts;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.ui.components.CodeCompletionPopup;
import root.ui.panelbuilders.RootPanelBuilder;
import root.ui.uibuilders.MenuBuilderUI;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;

@SpringBootApplication
public class Main implements UIEventObserver {

    public static final JFrame FRAME = new JFrame();

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    private CodeCompletionPopup codeCompletionPopup;



    public static void main(String[] args) {
        FlatNordIJTheme.setup();
        UIManager.put("TabbedPane.selectedBackground", new Color(71, 73, 99));
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false);
        builder.run(args);

    }

    public Main (MenuBuilderUI menuBuilderUI, RootPanelBuilder rootPanelBuilder, DirectoryChangesDetector directoryChangesDetector, ApplicationState applicationState, ProjectStructureNodesHandler projectStructureNodesHandler, ApplicationShortcuts applicationShortcuts, CodeCompletionPopup codeCompletionPopup) throws IOException {
        this.projectStructureNodesHandler = projectStructureNodesHandler;
        this.codeCompletionPopup = codeCompletionPopup;

        JMenuBar menu = menuBuilderUI.createMenu();
        FRAME.setJMenuBar(menu);
        FRAME.setExtendedState( FRAME.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        JPanel mainPanel = rootPanelBuilder.getMainPanel();
        FRAME.setContentPane(mainPanel);
        FRAME.setVisible(true);
        FRAME.addWindowFocusListener(directoryChangesDetector);
        FRAME.addWindowFocusListener(codeCompletionPopup);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationShortcuts.assignShortcuts(mainPanel);

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            for (Process runningProcess : applicationState.getRunningProcesses()) {
                runningProcess.destroy();
            }
        }));


    }


    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        if (eventType.equals(UIEventType.PROJECT_OPENED)){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) data;
            String title = projectStructureNodesHandler.getText(node);
            FRAME.setTitle(title);
        }
    }
}
