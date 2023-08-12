package root.core.process.commandline;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import root.core.uievents.UIEventType;
import root.core.uievents.UIEventsQueue;

import java.io.BufferedReader;
import java.io.IOException;

@Component
@Scope("prototype")
public class ProcessOutputReader implements Runnable {

    private BufferedReader bufferedReader;

    private UIEventsQueue uiEventsQueue;

    public ProcessOutputReader(UIEventsQueue uiEventsQueue) {
        this.uiEventsQueue = uiEventsQueue;
    }

    public void setBufferedReader(BufferedReader bufferedReader){
        this.bufferedReader = bufferedReader;
    }

    @Override
    public void run() {
        boolean hasMoreData = true;
        while (hasMoreData){
            try {
                String line = bufferedReader.readLine();
                if (line==null){
                    hasMoreData=false;
                }
                else{
                    uiEventsQueue.dispatchEvent(UIEventType.CONSOLE_DATA_AVAILABLE, line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
