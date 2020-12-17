package org.zalando.riptide.concurrent;

import lombok.AllArgsConstructor;
import lombok.With;
import org.zalando.riptide.concurrent.ThreadPoolExecutors.*;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.function.UnaryOperator;

import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.function.UnaryOperator.identity;
import static lombok.AccessLevel.PRIVATE;

@With(PRIVATE)
@AllArgsConstructor
final class ThreadPoolExecutorBuilder implements
        Start,
        FixedSize,
        ElasticSize, KeepAliveTime,
        QueueFirst, ScaleFirst,
        Threads, PreStart, RejectedExecutions,
        Build {

    private final Integer corePoolSize;
    private final Integer maximumPoolSize;
    private final Long keepAliveTime;
    private final TimeUnit unit;
    private final boolean allowCoreThreadTimeOut;
    private final BlockingQueue<Runnable> queue;
    private final ThreadFactory threadFactory;
    private final boolean preStartCoreThreads;
    private final RejectedExecutionHandler handler;

    private final UnaryOperator<BlockingQueue<Runnable>> queueProcessor;
    private final UnaryOperator<RejectedExecutionHandler> handlerProcessor;

    public ThreadPoolExecutorBuilder() {
        this(null,
                null,
                null,
                null,
                false,
                null,
                defaultThreadFactory(),
                false,
                new AbortPolicy(),
                identity(),
                identity());
    }

    @Override
    public FixedSize fixedSize(final int poolSize) {
        return withCorePoolSize(poolSize)
                .withMaximumPoolSize(poolSize)
                .withKeepAliveTime(0L)
                .withUnit(TimeUnit.NANOSECONDS);
    }

    @Override
    public ElasticSize elasticSize(
            final int corePoolSize, final int maximumPoolSize) {

        return withCorePoolSize(corePoolSize)
                .withMaximumPoolSize(maximumPoolSize);
    }

    @Override
    public ThreadPoolExecutorBuilder withoutQueue() {
        return withQueue(new SynchronousQueue<>());
    }

    @Override
    public ThreadPoolExecutorBuilder boundedQueue(final int queueSize) {
        return withQueue(new ArrayBlockingQueue<>(queueSize));
    }

    @Override
    public ThreadPoolExecutorBuilder unboundedQueue() {
        return withQueue(new LinkedBlockingQueue<>());
    }

    @Override
    public KeepAliveTime keepAlive(final long time, final TimeUnit unit) {
        return withKeepAliveTime(time).withUnit(unit);
    }

    @Override
    public QueueFirst queueFirst() {
        return this;
    }

    @Override
    public ScaleFirst scaleFirst() {
        if (corePoolSize == 0) {
            // ThreadPoolExecutor has scale-first support, but just for this
            return withCorePoolSize(maximumPoolSize)
                    .withAllowCoreThreadTimeOut(true);
        } else {
            return withQueueProcessor(WorkQueue::new)
                    .withHandlerProcessor(ReEnqueuePolicy::new);
        }
    }

    @Override
    public PreStart threadFactory(final ThreadFactory threadFactory) {
        return withThreadFactory(threadFactory);
    }

    @Override
    public RejectedExecutions preStartCoreThreads(
            final boolean preStartCoreThreads) {

        return withPreStartCoreThreads(preStartCoreThreads);
    }

    @Override
    public Build handler(final RejectedExecutionHandler handler) {
        return withHandler(handler);
    }

    @Override
    public ThreadPoolExecutor build() {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                queueProcessor.apply(queue),
                threadFactory,
                handlerProcessor.apply(handler)
        );

        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);

        if (preStartCoreThreads) {
            executor.prestartAllCoreThreads();
        }

        return executor;
    }

}
