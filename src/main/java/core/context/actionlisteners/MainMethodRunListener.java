package core.context.actionlisteners;

import core.Main;
import core.backend.*;
import core.dto.ApplicatonState;
import core.dto.MavenCommandResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO>  {

    private ProjectStructureSelectionContextDTO context;

    private JavaRunCommandBuilder javaRunCommandBuilder;

    private ThreadExecutor threadExecutor;


    private UIEventsQueue uiEventsQueue;

    private FileAutoSaver fileAutoSaver;

    private ApplicatonState applicatonState;

    private ProcessExecutor processExecutor;

    public MainMethodRunListener(JavaRunCommandBuilder javaRunCommandBuilder, ThreadExecutor threadExecutor, UIEventsQueue uiEventsQueue, FileAutoSaver fileAutoSaver, ApplicatonState applicatonState, ProcessExecutor processExecutor) {
        this.javaRunCommandBuilder = javaRunCommandBuilder;
        this.threadExecutor = threadExecutor;
        this.uiEventsQueue = uiEventsQueue;
        this.fileAutoSaver = fileAutoSaver;
        this.applicatonState = applicatonState;
        this.processExecutor = processExecutor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Running java application: "+ context.getSelectedFile().getName());
        fileAutoSaver.save();
        Set<File> classesToRecompile = applicatonState.getClassesToRecompile();
        List<String [] > commands = new ArrayList<>();
        threadExecutor.runTasksSequentially(()->getCommandsToCompileClasses(classesToRecompile, commands), ()->getCommandsToRunApplication(commands),
                processExecutor.executeCommands(commands)
                );

    }

    private void getCommandsToCompileClasses(Set<File> classes, List<String[]> commandsList){
        if (classes.isEmpty()){
            return;
        }

        String[] commands = javaRunCommandBuilder.createCommandForCompilingClass(classes);
        applicatonState.clearClassesToRecompile();
        commandsList.add(commands);
    }

    private void getCommandsToRunApplication(List<String[]> commands)  {
        File selectedFile = context.getSelectedFile();
        String[] commandsToRunMainClass = javaRunCommandBuilder.createCommandForRunningMainClass(selectedFile);
        commands.add(commandsToRunMainClass);
    }


    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }

}
