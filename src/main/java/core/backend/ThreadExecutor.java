package core.backend;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class ThreadExecutor {

    private CompletableFuture<Void> completableFuture;

    public void scheduleFirstTask(Runnable task){
        completableFuture = new CompletableFuture<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(executor);
        executorCompletionService.submit(()->{
            task.run();
            completableFuture.complete(null);
            return null;
        });

    }

    public CompletableFuture<Void> thenTask(Runnable runnable){
        if (completableFuture.isDone()){
            return CompletableFuture.runAsync(runnable);
        }
        else{
            return completableFuture.thenRun(runnable);
        }
    }

    public void scheduleIndependentTask (Runnable runnable){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runnable);
    }


}
