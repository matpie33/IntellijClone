package root.core.utility;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ThreadExecutor {

    private CompletableFuture<Void> mavenInitialTask;

    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public void addMavenInitialTask(Runnable task){


        if (mavenInitialTask == null || mavenInitialTask.isDone()){
            mavenInitialTask =  CompletableFuture.runAsync(task);
            mavenInitialTask.exceptionally(this::printStackTrace);
        }
        else{
            mavenInitialTask = mavenInitialTask.exceptionally(t-> {
                 task.run();
                 return null;
            });
            mavenInitialTask.exceptionally(this::printStackTrace);
        }

    }

    public void runTaskAfterMavenTaskFinished (Runnable... tasks){
        Iterator<Runnable> tasksIterator = Arrays.stream(tasks).iterator();
        Runnable firstTask = tasksIterator.next();
        CompletableFuture<Void> nextTask = mavenInitialTask.isDone()? CompletableFuture.runAsync(firstTask): mavenInitialTask.thenRun(firstTask);
        while (tasksIterator.hasNext()){
            nextTask = nextTask.thenRun(tasksIterator.next());
        }
        nextTask.exceptionally(this::printStackTrace);
    }

    private Void printStackTrace(Throwable t) {
        t.printStackTrace();
        return null;
    }

    public void scheduleIndependentTask (Runnable runnable){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runnable);
    }

    public void scheduleTask (Runnable runnable){
        threadPool.submit(runnable);
    }


    public void waitForMavenTask() throws ExecutionException, InterruptedException {
        mavenInitialTask.get();
    }
}
