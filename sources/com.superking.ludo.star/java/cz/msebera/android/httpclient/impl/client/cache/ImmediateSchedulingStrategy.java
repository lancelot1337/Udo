package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.util.Args;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class ImmediateSchedulingStrategy implements SchedulingStrategy {
    private final ExecutorService executor;

    public ImmediateSchedulingStrategy(CacheConfig cacheConfig) {
        this(new ThreadPoolExecutor(cacheConfig.getAsynchronousWorkersCore(), cacheConfig.getAsynchronousWorkersMax(), (long) cacheConfig.getAsynchronousWorkerIdleLifetimeSecs(), TimeUnit.SECONDS, new ArrayBlockingQueue(cacheConfig.getRevalidationQueueSize())));
    }

    ImmediateSchedulingStrategy(ExecutorService executor) {
        this.executor = executor;
    }

    public void schedule(AsynchronousValidationRequest revalidationRequest) {
        Args.notNull(revalidationRequest, "AsynchronousValidationRequest");
        this.executor.execute(revalidationRequest);
    }

    public void close() {
        this.executor.shutdown();
    }

    void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        this.executor.awaitTermination(timeout, unit);
    }
}
