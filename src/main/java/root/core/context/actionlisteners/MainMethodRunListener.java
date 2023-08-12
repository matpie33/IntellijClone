package root.core.context.actionlisteners;

import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
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

    private ApplicationState applicationState;

    private ProcessExecutor processExecutor;

    public MainMethodRunListener(JavaRunCommandBuilder javaRunCommandBuilder, ThreadExecutor threadExecutor, UIEventsQueue uiEventsQueue, FileAutoSaver fileAutoSaver, ApplicationState applicationState, ProcessExecutor processExecutor) {
        this.javaRunCommandBuilder = javaRunCommandBuilder;
        this.threadExecutor = threadExecutor;
        this.uiEventsQueue = uiEventsQueue;
        this.fileAutoSaver = fileAutoSaver;
        this.applicationState = applicationState;
        this.processExecutor = processExecutor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Running java application: "+ context.getSelectedFile().getName());
        fileAutoSaver.save();
        Set<File> classesToRecompile = applicationState.getClassesToRecompile();
        List<String[]> commands = getCommandsToCompileAndRunApplication(classesToRecompile);
        threadExecutor.runTaskAfterMavenTaskFinished(processExecutor.executeCommands(commands));
        applicationState.clearClassesToRecompile();

    }

    private List<String[]> getCommandsToCompileAndRunApplication(Set<File> classesToRecompile) {
        List<String [] > commands = new ArrayList<>();
        String[] commandsToCompileClasses = getCommandsToCompileClasses(classesToRecompile);
        String[] commandsToRunApplication = getCommandsToCompileAndRunApplication();
        commands.add(commandsToCompileClasses);
        commands.add(commandsToRunApplication);
        return commands;
    }

    private String[] getCommandsToCompileClasses(Set<File> classes){
        if (classes.isEmpty()){
            return new String[0];
        }

        return javaRunCommandBuilder.createCommandForCompilingClass(classes);
    }

    private String[] getCommandsToCompileAndRunApplication()  {
        File selectedFile = context.getSelectedFile();
        return javaRunCommandBuilder.createCommandForRunningMainClass(selectedFile);
    }


    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }

}
