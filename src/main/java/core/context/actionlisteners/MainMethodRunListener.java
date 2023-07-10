package core.context.actionlisteners;

import core.Main;
import core.backend.*;
import core.dto.ErrorDTO;
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
import java.util.concurrent.CompletableFuture;

@Component
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO> implements ApplicationContextAware {

    private ProjectStructureSelectionContextDTO context;

    private JavaRunCommandBuilder javaRunCommandBuilder;

    private ThreadExecutor threadExecutor;


    private UIEventsQueue uiEventsQueue;

    private MavenCommandExecutor mavenCommandExecutor;

    private FileAutoSaver fileAutoSaver;
    private ApplicationContext applicationContext;

    public MainMethodRunListener(JavaRunCommandBuilder javaRunCommandBuilder, ThreadExecutor threadExecutor, UIEventsQueue uiEventsQueue, MavenCommandExecutor mavenCommandExecutor, FileAutoSaver fileAutoSaver) {
        this.javaRunCommandBuilder = javaRunCommandBuilder;
        this.threadExecutor = threadExecutor;
        this.uiEventsQueue = uiEventsQueue;
        this.mavenCommandExecutor = mavenCommandExecutor;
        this.fileAutoSaver = fileAutoSaver;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Running java application: "+ context.getSelectedFile().getName());
        fileAutoSaver.save();
        threadExecutor.runTasksSequentially(this::executeMavenCleanInstall, this::executeJavaRunCommand);
    }

    private void executeMavenCleanInstall() {
        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Executing maven clean install");
        MavenCommandResultDTO result = mavenCommandExecutor.runCommandInConsole("clean install");
        if (!result.isSuccess()){
            JOptionPane.showMessageDialog(Main.FRAME, "Failed to run maven, check console");
            uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, result.getOutput());
            throw new RuntimeException("Maven command failed");
        }
    }

    private void executeJavaRunCommand()  {
        File selectedFile = context.getSelectedFile();
        String[] commands = javaRunCommandBuilder.build(selectedFile.getName());

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            Process process = processBuilder.start();
            addStreamReader(process.getInputStream());
            addStreamReader(process.getErrorStream());

            process.onExit().whenComplete((res, ex)->{
                if (res.exitValue() !=0){
                    uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO("Error running java command", new IllegalArgumentException("Wrong argument to process builder")));
                }
            });
        } catch (IOException  e) {
            uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO("Error running java command", e));

        }
    }

    private void addStreamReader(InputStream inputStream) {
        BufferedReader bufferedInputReader = new BufferedReader(new InputStreamReader(inputStream));
        ProcessOutputReader outputReader = applicationContext.getBean(ProcessOutputReader.class);
        outputReader.setBufferedReader(bufferedInputReader);
        threadExecutor.scheduleIndependentTask(outputReader);
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
