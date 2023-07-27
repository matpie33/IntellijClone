package core.menuitemlisteners;

import core.dialogbuilders.SettingsDialogBuilder;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class SettingsMenuItemListener extends AbstractAction implements MenuItemListener {

    private SettingsDialogBuilder settingsDialogBuilder;

    public SettingsMenuItemListener(SettingsDialogBuilder settingsDialogBuilder) {
        this.settingsDialogBuilder = settingsDialogBuilder;
    }

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JDialog dialog = settingsDialogBuilder.getDialog();
        dialog.setVisible(true);
    }
}
