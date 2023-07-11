package core.backend;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;

@Component
public class ThreadExecutor {

    private CompletableFuture<Void> mavenReadClassPathTask;

    public void addReadClassPathMaventask(Runnable task){
        mavenReadClassPathTask = CompletableFuture.runAsync(task);
        mavenReadClassPathTask.exceptionally(this::printStackTrace);
    }

    public void runTasksSequentially (Runnable... tasks){
        Iterator<Runnable> tasksIterator = Arrays.stream(tasks).iterator();
        Runnable firstTask = tasksIterator.next();
        CompletableFuture<Void> nextTask = mavenReadClassPathTask.isDone()? CompletableFuture.runAsync(firstTask): mavenReadClassPathTask.thenRun(firstTask);
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


}
