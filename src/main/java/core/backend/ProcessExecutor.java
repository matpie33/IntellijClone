package core.backend;

import core.dto.ApplicatonState;
import core.dto.ErrorDTO;
import core.uievents.UIEventType;
import core.uievents.UIEventsQueue;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Component
public class ProcessExecutor implements ApplicationContextAware {

    private ThreadExecutor threadExecutor;

    private ProcessBuilder processBuilder = new ProcessBuilder();

    private ApplicatonState applicatonState;
    private ApplicationContext applicationContext;

    private UIEventsQueue uiEventsQueue;

    public ProcessExecutor(ThreadExecutor threadExecutor, ApplicatonState applicatonState, UIEventsQueue uiEventsQueue) {
        this.threadExecutor = threadExecutor;
        this.applicatonState = applicatonState;
        this.uiEventsQueue = uiEventsQueue;
    }

    public Runnable executeCommands(List<String[]> commands){
        processBuilder.directory(Path.of( applicatonState.getProjectPath() +
                File.separator + applicatonState.getProjectRootDirectoryName()).toFile());

        return () -> {
            for (String[] command : commands) {
                if (command.length==0){
                    continue;
                }
                processBuilder.command(command);
                try {
                    Process process = processBuilder.start();
                    addStreamReader(process.getInputStream());
                    addStreamReader(process.getErrorStream());

                    process.onExit().whenComplete((res, ex)->{
                        if (res.exitValue() !=0){
                            uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO("Error running java command", new IllegalArgumentException("Wrong argument to process builder")));
                        }
                    });
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };

    }

        private void addStreamReader(InputStream inputStream) {
            BufferedReader bufferedInputReader = new BufferedReader(new InputStreamReader(inputStream));
            ProcessOutputReader outputReader = applicationContext.getBean(ProcessOutputReader.class);
            outputReader.setBufferedReader(bufferedInputReader);
            threadExecutor.scheduleIndependentTask(outputReader);
        }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
