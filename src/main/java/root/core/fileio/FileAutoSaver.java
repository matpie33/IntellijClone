package root.core.fileio;

import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

@Component
public class FileAutoSaver {

    private final Timer saveTimer;
    private final int firstCheckDelay;
    private final int checkPeriod;
    private final FileIO fileIO;

    private int idleTimeBeforeSave = 2000;

    private long lastTextChangeTime;

    private boolean isDirty = false;
    private String currentText;


    public FileAutoSaver(FileIO fileIO) {
        this.fileIO = fileIO;
        saveTimer = new Timer();
        firstCheckDelay = 1000;
        checkPeriod = 1000;
        saveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (isDirty && now- lastTextChangeTime >idleTimeBeforeSave){
                    saveInternal();
                }
            }
        }, firstCheckDelay, checkPeriod);
    }

    private void saveInternal() {
        isDirty = false;
        fileIO.save(currentText);
    }

    public void save (){
        if (isDirty){
            saveInternal();
            currentText=null;
        }
    }

    public void textInserted(String currentText){
        lastTextChangeTime = System.currentTimeMillis();
        isDirty = true;
        this.currentText = currentText;
    }

}
