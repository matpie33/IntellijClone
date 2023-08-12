package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.core.dto.FileReadResultDTO;
import root.core.nodehandling.ProjectStructureNodesHandler;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;

@Component
public class NavigationPanelBuilder implements UIEventObserver {

    private IconsPanelBuilder iconsPanelBuilder;
    private JLabel navigation;

    private ProjectStructureNodesHandler projectStructureNodesHandler;

    public NavigationPanelBuilder(IconsPanelBuilder iconsPanelBuilder, ProjectStructureNodesHandler projectStructureNodesHandler) {
        this.iconsPanelBuilder = iconsPanelBuilder;
        this.projectStructureNodesHandler = projectStructureNodesHandler;
    }

    public JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout(5, 2));
        navigationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        navigation = new JLabel("");
        navigationPanel.add(navigation, BorderLayout.WEST);
        JPanel iconsPanel = iconsPanelBuilder.createIconsPanel();
        navigationPanel.add(iconsPanel, BorderLayout.EAST);

        return navigationPanel;

    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case FILE_OPENED_FOR_EDIT:
                FileReadResultDTO resultDTO = (FileReadResultDTO)data;
                String pathFromRoot = resultDTO.getPathFromRoot();
                navigation.setText(pathFromRoot.replace(File.separator, ">"));
                break;
            case PROJECT_OPENED:
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) data;
                navigation.setText(projectStructureNodesHandler.getText(rootNode));
                break;
        }
    }
}
