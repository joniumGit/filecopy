package dev.jonium.filecopy.impl;

import dev.jonium.filecopy.CopyOperation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Logger;

/**
 * Simple Implementation
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class CopyOperationImpl implements CopyOperation {

    Logger log = Logger.getLogger(getClass().getName());

    CountDownLatch starter;
    Future<?> reader;
    Future<?> writer;
    ExecutorService checker = Executors.newSingleThreadExecutor();

    AtomicLong startTime = new AtomicLong();
    AtomicReference<LongConsumer> success = new AtomicReference<>();
    AtomicReference<Consumer<Exception>> fail = new AtomicReference<>();
    AtomicReference<Runnable> cancelled = new AtomicReference<>();

    public CopyOperationImpl(CountDownLatch starter, Future<?> reader, Future<?> writer) {
        this.starter = starter;
        this.reader = reader;
        this.writer = writer;
        checker.submit(this::check);
    }

    private void runTask(LongConsumer task) {
        if (task != null) {
            var end = System.currentTimeMillis();
            task.accept(end - startTime.get());
        }
    }

    private void run(Runnable task) {
        if (task != null) {
            task.run();
        }
    }

    private void check() {
        try {
            reader.get();
            writer.get();
            runTask(success.get());
            log.info("Copy succeeded");
        } catch (CancellationException e) {
            run(cancelled.get());
            log.info("Cancelled copy operation");
        } catch (InterruptedException e) {
            run(cancelled.get());
            Thread.currentThread().interrupt();
            log.warning("Copy operation interrupted");
        } catch (ExecutionException e) {
            var ex = e.getCause() instanceof Exception ? (Exception) e.getCause() : e;
            var task = fail.get();
            if (task != null) {
                task.accept(ex);
            }
            log.warning(() -> "Copy operation encountered an exception (" + ex.getClass() + ":" + ex.getMessage() + ")");
        }
        checker.shutdown();
    }

    @Override
    public void onSuccess(LongConsumer onSuccess) {
        success.set(onSuccess);
    }

    @Override
    public void onFailure(Consumer<Exception> onFailure) {
        fail.set(onFailure);
    }

    @Override
    public void onCancel(Runnable onCancel) {
        cancelled.set(onCancel);
    }

    @Override
    public void cancel() {
        reader.cancel(true);
        writer.cancel(true);
        checker.shutdownNow();
    }

    @Override
    public void start() {
        startTime.set(System.currentTimeMillis());
        log.info("Starting a copy operation...");
        starter.countDown();
    }

}
