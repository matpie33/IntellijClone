package core.context.actionlisteners;

import core.backend.JavaRunCommandBuilder;
import core.backend.ThreadExecutor;
import core.dto.ProjectStructureSelectionContextDTO;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

@Component
public class MainMethodRunListener extends ContextAction<ProjectStructureSelectionContextDTO>{

    private ProjectStructureSelectionContextDTO context;

    private JavaRunCommandBuilder javaRunCommandBuilder;

    private ThreadExecutor threadExecutor;

    public MainMethodRunListener(JavaRunCommandBuilder javaRunCommandBuilder, ThreadExecutor threadExecutor) {
        this.javaRunCommandBuilder = javaRunCommandBuilder;
        this.threadExecutor = threadExecutor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        threadExecutor.scheduleNewTask(this::executeJavaRunCommand);
    }

    private void executeJavaRunCommand()  {
        File selectedFile = context.getSelectedFile();
        String[] commands = javaRunCommandBuilder.build(selectedFile.getName());

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContext(ProjectStructureSelectionContextDTO context) {
        this.context = context;
    }
}
