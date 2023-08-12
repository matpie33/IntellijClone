package root.core.utility;

import javax.swing.*;
import java.awt.*;

public class UIUtilities {

    public static JPanel addElementsAsSingleLine (JComponent... components){
        JPanel panel = new JPanel();
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        for (JComponent component : components) {
            panel.add(component, BorderLayout.EAST);
        }
        return panel;
    }

}
