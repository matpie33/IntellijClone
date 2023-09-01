package root.ui.panelbuilders;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class IconsPanelBuilder {

    public JPanel createIconsPanel() {
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        iconsPanel.add(createIcon("/icons/build.png", "build"));
        iconsPanel.add(createIcon("/icons/run.png", "run"));
        iconsPanel.add(createIcon("/icons/debug.png", "debug"));
        iconsPanel.add(createIcon("/icons/runWithCoverage.png", "Run with coverage"));
        iconsPanel.add(createIcon("/icons/doProfile.png", "Profile <class> with 'Intellij profiler'"));
        iconsPanel.add(createIcon("/icons/stopDisabled.png", "Stop application"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(new JLabel("Git: "));
        iconsPanel.add(createIcon("/icons/gitUpdate.png", "Update project"));
        iconsPanel.add(createIcon("/icons/gitCommit.png", "Git commit"));
        iconsPanel.add(createIcon("/icons/gitPush.png", "Git push"));
        iconsPanel.add(createIcon("/icons/gitShowHistory.png", "Show history"));
        iconsPanel.add(createIcon("/icons/rollback.png", "Rollback"));
        iconsPanel.add(createSeparator());
        iconsPanel.add(createIcon("/icons/searchEverywhere.png", "Search everywhere"));
        iconsPanel.add(createIcon("/icons/updatesAvailable.png", "Updates available"));
        iconsPanel.add(createIcon("/icons/spaceIcon.png", "Space: the integrated team environment"));
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
