package core;

import core.panelbuilders.NavigationPanelBuilder;
import core.panelbuilders.SplitPanesBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class EditorPanels {

    private JPanel mainPanel;

    private SplitPanesBuilder splitPanesBuilder;

    private NavigationPanelBuilder navigationPanelBuilder;


    public EditorPanels(SplitPanesBuilder splitPanesBuilder, NavigationPanelBuilder navigationPanelBuilder) {
        this.splitPanesBuilder = splitPanesBuilder;
        this.navigationPanelBuilder = navigationPanelBuilder;
    }

    @PostConstruct
    public void init (){
        createMainPanel();
        JPanel navigationPanel = navigationPanelBuilder.createNavigationPanel();
        JPanel splitPanes = splitPanesBuilder.createSplitPanesRootPanel();
        mainPanel.add(navigationPanel, BorderLayout.PAGE_START);
        mainPanel.add(splitPanes, BorderLayout.CENTER);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


}
