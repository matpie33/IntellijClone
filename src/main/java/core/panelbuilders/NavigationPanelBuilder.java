package core.panelbuilders;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

@Component
public class NavigationPanelBuilder {

    private IconsPanelBuilder iconsPanelBuilder;

    public NavigationPanelBuilder(IconsPanelBuilder iconsPanelBuilder) {
        this.iconsPanelBuilder = iconsPanelBuilder;
    }

    public JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout(5, 2));
        navigationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JLabel navigation = new JLabel("src>main>java>core>MainUI>createNavigationPanel");
        navigationPanel.add(navigation, BorderLayout.WEST);
        JPanel iconsPanel = iconsPanelBuilder.createIconsPanel();
        navigationPanel.add(iconsPanel, BorderLayout.EAST);

        return navigationPanel;

    }
}
