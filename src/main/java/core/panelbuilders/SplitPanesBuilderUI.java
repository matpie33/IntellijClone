package core.panelbuilders;

import core.contextMenu.ContextMenuValues;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class SplitPanesBuilderUI {

    private ClassStructurePanelBuilderUI classStructurePanelBuilderUI;

    private ProjectStructurePanelBuilder projectStructurePanelBuilder;

    private FileEditorPanelBuilder fileEditorPanelBuilder;

    private ConsolePanelBuilder consolePanelBuilder;


    public SplitPanesBuilderUI(ClassStructurePanelBuilderUI classStructurePanelBuilderUI, ProjectStructurePanelBuilder projectStructurePanelBuilder, FileEditorPanelBuilder fileEditorPanelBuilder, ConsolePanelBuilder consolePanelBuilder) {
        this.classStructurePanelBuilderUI = classStructurePanelBuilderUI;
        this.projectStructurePanelBuilder = projectStructurePanelBuilder;
        this.fileEditorPanelBuilder = fileEditorPanelBuilder;
        this.consolePanelBuilder = consolePanelBuilder;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        JPanel classStructurePanel = classStructurePanelBuilderUI.getPanel();
        JPanel fileEditorPanel = fileEditorPanelBuilder.getPanel();
        JPanel projectStructurePanel = projectStructurePanelBuilder.getPanel();
        JPanel consolePanel = consolePanelBuilder.getPanel();

        JSplitPane horizontalLeftPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectStructurePanel, fileEditorPanel);
        JSplitPane horizontalRightPart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, horizontalLeftPart, classStructurePanel);
        JSplitPane rootSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalRightPart, consolePanel);

        horizontalLeftPart.setResizeWeight(0.3);
        horizontalRightPart.setResizeWeight(0.7);
        rootSplitpane.setResizeWeight(0.8);

        rootPanel.add(rootSplitpane, BorderLayout.CENTER);
        return  rootPanel;
    }



}
