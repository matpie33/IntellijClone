package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicatonState;
import root.core.dto.ProjectStructureSelectionContextDTO;
import root.core.fileio.FileAutoSaver;
import root.core.process.commandline.JavaRunCommandBuilder;
import root.core.process.commandline.ProcessExecutor;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.core.utility.ThreadExecutor;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
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
