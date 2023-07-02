package core;

import core.uibuilders.IconsPanelBuilderUI;
import core.uibuilders.NavigationPanelBuilderUI;
import core.uibuilders.SplitPanesBuilderUI;
import core.uievents.UIEventHandler;
import core.uievents.UIEventType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EditorPanels {

    private JPanel mainPanel;

    private SplitPanesBuilderUI splitPanesBuilderUI;

    private NavigationPanelBuilderUI navigationPanelBuilderUI;


    public EditorPanels(SplitPanesBuilderUI splitPanesBuilderUI, NavigationPanelBuilderUI navigationPanelBuilderUI) {
        this.splitPanesBuilderUI = splitPanesBuilderUI;
        this.navigationPanelBuilderUI = navigationPanelBuilderUI;
    }

    @PostConstruct
    public void init (){
        createMainPanel();
        JPanel navigationPanel = navigationPanelBuilderUI.createNavigationPanel();
        JPanel splitPanes = splitPanesBuilderUI.createSplitPanesRootPanel();
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
