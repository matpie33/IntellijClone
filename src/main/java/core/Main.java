package core;

import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import core.backend.DirectoryChangesDetector;
import core.uibuilders.MenuBuilderUI;
import core.uievents.UIEventObserver;
import core.uievents.UIEventType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

@SpringBootApplication
public class Main implements UIEventObserver {

    public static final JFrame FRAME = new JFrame();

    private DirectoryChangesDetector directoryChangesDetector;

    public static void main(String[] args) {
        FlatNordIJTheme.setup();
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false);
        builder.run(args);

    }

    public Main (MenuBuilderUI menuBuilderUI, EditorPanels editorPanels, DirectoryChangesDetector directoryChangesDetector) throws IOException {
        this.directoryChangesDetector = directoryChangesDetector;

        JMenuBar menu = menuBuilderUI.createMenu();
        FRAME.setJMenuBar(menu);
        FRAME.setExtendedState( FRAME.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        FRAME.setContentPane(editorPanels.getMainPanel());
        FRAME.setVisible(true);
        FRAME.addWindowFocusListener(directoryChangesDetector);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }


    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        if (eventType.equals(UIEventType.PROJECT_OPENED)){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) data;
            FRAME.setTitle(node.getUserObject().toString());
        }
    }
}
