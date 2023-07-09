package core.backend;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class ThreadExecutor {

    private CompletableFuture<Object> completableFuture;

    public void scheduleFirstTask(Runnable task){
        completableFuture = new CompletableFuture<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Object> executorCompletionService = new ExecutorCompletionService<>(executor);
        executorCompletionService.submit(()->{
            task.run();
            completableFuture.complete(null);
            return null;
        });

    }

    public void scheduleNewTask(Runnable runnable){
        completableFuture.thenRun(runnable);
    }


}
