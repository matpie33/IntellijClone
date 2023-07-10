package core.context.actionlisteners;

import core.Main;
import core.backend.*;
import core.dto.MavenCommandResultDTO;
import core.dto.ProjectStructureSelectionContextDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.concurrent.CompletableFuture;

@Component
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    private JavaRunCommandBuilder javaRunCommandBuilder;

    private ThreadExecutor threadExecutor;

    private ProcessOutputReader processOutputReader;

    private UIEventsQueue uiEventsQueue;

    private MavenCommandExecutor mavenCommandExecutor;

    private FileAutoSaver fileAutoSaver;

    public MainMethodRunListener(JavaRunCommandBuilder javaRunCommandBuilder, ThreadExecutor threadExecutor, ProcessOutputReader processOutputReader, UIEventsQueue uiEventsQueue, MavenCommandExecutor mavenCommandExecutor, FileAutoSaver fileAutoSaver) {
        this.javaRunCommandBuilder = javaRunCommandBuilder;
        this.threadExecutor = threadExecutor;
        this.processOutputReader = processOutputReader;
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
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            processOutputReader.setBufferedReader(bufferedReader);
            threadExecutor.scheduleIndependentTask(processOutputReader);
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
