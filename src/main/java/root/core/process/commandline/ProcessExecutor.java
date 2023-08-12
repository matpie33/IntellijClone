package root.core.process.commandline;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import root.core.dto.ApplicationState;
import root.core.dto.ErrorDTO;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;
import root.core.utility.ThreadExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class ProcessExecutor implements ApplicationContextAware {

    private ThreadExecutor threadExecutor;

    private ProcessBuilder processBuilder = new ProcessBuilder();

    private ApplicationState applicationState;
    private ApplicationContext applicationContext;

    private UIEventsQueue uiEventsQueue;

    private ProcessOutputReader inputStreamReader;
    private ProcessOutputReader errorStreamReader;

    public ProcessExecutor(ThreadExecutor threadExecutor, ApplicationState applicationState, UIEventsQueue uiEventsQueue) {
        this.threadExecutor = threadExecutor;
        this.applicationState = applicationState;
        this.uiEventsQueue = uiEventsQueue;
    }


    public void executeCommands(List<String[]> commands){
        processBuilder.directory( applicationState.getProjectPath());

        uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, "Running java application.");
        for (String[] command : commands) {
            if (command.length==0){
                continue;
            }
            processBuilder.command(command);
            try {
                Process process = processBuilder.start();
                applicationState.addRunningProcess(process);
                addStreamReader(process.getInputStream(), inputStreamReader);
                addStreamReader(process.getErrorStream(), errorStreamReader);

                process.onExit().whenComplete((processLocal, ex)->{
                    applicationState.removeRunningProcess(processLocal);
                    if (processLocal.exitValue() !=0){
                        uiEventsQueue.dispatchEvent(UIEventType.ERROR_OCCURRED, new ErrorDTO("Error running java command", new IllegalArgumentException("Wrong argument to process builder")));
                    }
                });
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }

        private void addStreamReader(InputStream inputStream, ProcessOutputReader processOutputReader) {
            BufferedReader bufferedInputReader = new BufferedReader(new InputStreamReader(inputStream));
            processOutputReader.setBufferedReader(bufferedInputReader);
            threadExecutor.runTaskInMainPoolAfterMavenTaskDone(processOutputReader);
        }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        inputStreamReader = applicationContext.getBean(ProcessOutputReader.class);
        errorStreamReader = applicationContext.getBean(ProcessOutputReader.class);
    }
}
