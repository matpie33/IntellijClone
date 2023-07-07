package core;

import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import core.backend.DirectoryChangesDetector;
import core.uibuilders.MenuBuilderUI;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.io.IOException;

@SpringBootApplication
public class Main {

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


    }


}
