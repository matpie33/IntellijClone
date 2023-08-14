package root.core.utility;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ThreadExecutor {

    private ExecutorService jdkRelatedPool = Executors.newFixedThreadPool(5);
    private ExecutorService mainPool = Executors.newFixedThreadPool(5);
    private CompletableFuture<Void> mavenTask;


    public void runTasksInMainPool(Runnable... tasks){
        CompletableFuture [] futures = new CompletableFuture[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Runnable task = tasks[i];
            CompletableFuture<Void> future = CompletableFuture.runAsync(task, mainPool).exceptionally(this::printStackTrace);

            futures[i] = future;
        }
        mavenTask = CompletableFuture.allOf(futures);
    }

    public void runTaskInJdkPoolAfterMavenTaskDone(Runnable task){
        mavenTask.thenRunAsync(task, jdkRelatedPool).exceptionally(this::printStackTrace);
    }

    public void runTaskInMainPoolAfterMavenTaskDone(Runnable task){
        mavenTask.thenRunAsync(task, mainPool).exceptionally(this::printStackTrace);
    }

    private Void printStackTrace(Throwable t) {
        t.getCause().printStackTrace();
        return null;
    }


    public boolean isMavenCommandRunning() {
        return !mavenTask.isDone();
    }
}
