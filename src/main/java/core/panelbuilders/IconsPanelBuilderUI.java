package core.panelbuilders;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class IconsPanelBuilderUI {

    public JPanel createIconsPanel() {
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        iconsPanel.add(createIcon( "/build.png", "build"));
        iconsPanel.add(createIcon( "/run.png", "run"));
        iconsPanel.add(createIcon( "/debug.png", "debug"));
        iconsPanel.add(createIcon( "/runWithCoverage.png", "Run with coverage"));
        iconsPanel.add(createIcon( "/doProfile.png", "Profile <class> with 'Intellij profiler'"));
        iconsPanel.add(createIcon( "/stopDisabled.png", "Stop application"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(new JLabel("Git: "));
        iconsPanel.add(createIcon( "/gitUpdate.png", "Update project"));
        iconsPanel.add(createIcon( "/gitCommit.png", "Git commit"));
        iconsPanel.add(createIcon( "/gitPush.png", "Git push"));
        iconsPanel.add(createIcon( "/gitShowHistory.png", "Show history"));
        iconsPanel.add(createIcon( "/rollback.png", "Rollback"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(createIcon( "/searchEverywhere.png", "Search everywhere"));
        iconsPanel.add(createIcon( "/updatesAvailable.png", "Updates available"));
        iconsPanel.add(createIcon( "/spaceIcon.png", "Space: the integrated team environment"));
        return iconsPanel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 16));
        return separator;
    }

    private JButton createIcon(String path, String tooltip) {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(path));
        JButton button = new JButton(imageIcon);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setOpaque(true);
        button.setToolTipText(tooltip);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                int r = 210;
                button.setBackground(new Color(r, r, r));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(null);
            }
        });
        return button;
    }

}
