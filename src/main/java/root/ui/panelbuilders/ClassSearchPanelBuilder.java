package root.ui.panelbuilders;

import org.springframework.stereotype.Component;
import root.Main;
import root.core.dto.ClassSuggestionDTO;
import root.core.dto.FileReadResultDTO;
import root.core.fileio.AbsoluteClassNameToFileConverter;
import root.core.fileio.ProjectFileOpener;
import root.core.keylisteners.ClassSearchKeyListener;
import root.core.keylisteners.ListNavigationKeyListener;
import root.core.uievents.UIEventObserver;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

@Component
public class ClassSearchPanelBuilder implements UIEventObserver {

    private JList<ClassSuggestionDTO> resultList;
    private JPanel panel;
    private JTextField inputField;
    private DefaultListModel<ClassSuggestionDTO> resultListModel;

    private ClassSearchKeyListener classSearchKeyListener;
    private JScrollPane resultListScrollPane;

    private AbsoluteClassNameToFileConverter absoluteClassNameToFileConverter;

    private ProjectFileOpener projectFileOpener;

    private UIEventsQueue uiEventsQueue;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;

    private JPopupMenu popupMenu;


    public ClassSearchPanelBuilder(ClassSearchKeyListener classSearchKeyListener, AbsoluteClassNameToFileConverter absoluteClassNameToFileConverter, ProjectFileOpener projectFileOpener, UIEventsQueue uiEventsQueue) {
        this.classSearchKeyListener = classSearchKeyListener;
        this.absoluteClassNameToFileConverter = absoluteClassNameToFileConverter;
        this.projectFileOpener = projectFileOpener;
        this.uiEventsQueue = uiEventsQueue;
        popupMenu = new JPopupMenu();
    }

    @PostConstruct
    public void init (){
        panel = new JPanel();
        createElements();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.PAGE_START);
        panel.add(resultListScrollPane, BorderLayout.CENTER);
    }

    public void focusInputField (){
        inputField.requestFocusInWindow();
    }

    private void createElements (){

        createList();
        createInputField();
    }

    private void createInputField() {
        inputField = new JTextField(30);
        inputField.addKeyListener(classSearchKeyListener);
        inputField.addKeyListener(new ListNavigationKeyListener(resultList));
    }

    private void createList() {
        resultListModel = new DefaultListModel<>();
        resultList = new JList<>(resultListModel);
        resultList.setFocusable(false);

        resultListScrollPane = new JScrollPane(resultList);
    }

    public void showPopup(){
        popupMenu.add(panel);
        java.awt.Component source = Main.FRAME.getContentPane();
        int width = source.getWidth();
        int height = source.getHeight();
        popupMenu.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        popupMenu.requestFocusInWindow();
        SwingUtilities.invokeLater(this::focusInputField);
        popupMenu.show(source, width/2 - WIDTH/2,height/2 -HEIGHT/2);
    }

    @Override
    public void handleEvent(UIEventType eventType, Object data) {
        switch (eventType){
            case CLASS_NAMES_FILTERED:
                resultListModel.clear();
                List<ClassSuggestionDTO> filteredClasses = (List<ClassSuggestionDTO>)data;
                filteredClasses.forEach(resultListModel::addElement);
                resultList.setSelectedIndex(0);
                break;
            case CLASS_CHOSEN_TO_OPEN:
                popupMenu.setVisible(false);
                inputField.selectAll();
                ClassSuggestionDTO classDTO = resultListModel.get(resultList.getSelectedIndex());
                File file = absoluteClassNameToFileConverter.convertToFile(classDTO);
                FileReadResultDTO fileReadResultDTO = projectFileOpener.readFile(classDTO.getClassOrigin(), file.toPath());
                uiEventsQueue.dispatchEvent(UIEventType.FILE_OPENED_FOR_EDIT, fileReadResultDTO);
        }
    }
}
