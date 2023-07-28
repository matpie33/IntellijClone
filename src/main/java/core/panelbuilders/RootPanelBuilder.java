package core.panelbuilders;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class RootPanelBuilder {

    private JPanel mainPanel;

    private SplitPanesBuilder splitPanesBuilder;

    private NavigationPanelBuilder navigationPanelBuilder;

    private BottomPanelBuilder bottomPanelBuilder;


    public RootPanelBuilder(SplitPanesBuilder splitPanesBuilder, NavigationPanelBuilder navigationPanelBuilder, BottomPanelBuilder bottomPanelBuilder) {
        this.splitPanesBuilder = splitPanesBuilder;
        this.navigationPanelBuilder = navigationPanelBuilder;
        this.bottomPanelBuilder = bottomPanelBuilder;
    }

    @PostConstruct
    public void init (){
        createMainPanel();
        JPanel navigationPanel = navigationPanelBuilder.createNavigationPanel();
        JPanel splitPanes = splitPanesBuilder.createSplitPanesRootPanel();
        JPanel bottomPanel = bottomPanelBuilder.getMainPanel();

        mainPanel.add(navigationPanel, BorderLayout.PAGE_START);
        mainPanel.add(splitPanes, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


}
