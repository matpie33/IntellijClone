package core.panelbuilders;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class SplitPanesBuilder {

    private ClassStructurePanelBuilder classStructurePanelBuilder;

    private ProjectStructurePanelBuilder projectStructurePanelBuilder;

    private FileEditorPanelBuilder fileEditorPanelBuilder;

    private ConsolePanelBuilder consolePanelBuilder;


    public SplitPanesBuilder(ClassStructurePanelBuilder classStructurePanelBuilder, ProjectStructurePanelBuilder projectStructurePanelBuilder, FileEditorPanelBuilder fileEditorPanelBuilder, ConsolePanelBuilder consolePanelBuilder) {
        this.classStructurePanelBuilder = classStructurePanelBuilder;
        this.projectStructurePanelBuilder = projectStructurePanelBuilder;
        this.fileEditorPanelBuilder = fileEditorPanelBuilder;
        this.consolePanelBuilder = consolePanelBuilder;
    }

    public JPanel createSplitPanesRootPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        JPanel classStructurePanel = classStructurePanelBuilder.getPanel();
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
