package core.panelbuilders;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

@Component
public class NavigationPanelBuilderUI {

    private IconsPanelBuilderUI iconsPanelBuilderUI;

    public NavigationPanelBuilderUI(IconsPanelBuilderUI iconsPanelBuilderUI) {
        this.iconsPanelBuilderUI = iconsPanelBuilderUI;
    }

    public JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout(5, 2));
        navigationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JLabel navigation = new JLabel("src>main>java>core>MainUI>createNavigationPanel");
        navigationPanel.add(navigation, BorderLayout.WEST);
        JPanel iconsPanel = iconsPanelBuilderUI.createIconsPanel();
        navigationPanel.add(iconsPanel, BorderLayout.EAST);

        return navigationPanel;

    }
}
