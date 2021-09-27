package dev.jonium.filecopy.ui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Simple progress observing helper tracking the file sizes of source and target
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class ProgressObserver {

    Path from;
    Path to;
    /**
     * Consumer for file copy progress.
     * This will be called in the tracking thread, so manipulating JavaFX
     * components needs the operations to be wrapped in {@link javafx.application.Platform#runLater(Runnable)}
     */
    Consumer<Double> progressConsumer;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicReference<Future<?>> task = new AtomicReference<>();

    /**
     * This gives a small delay to each loop
     */
    @SneakyThrows(InterruptedException.class)
    private void waitABit() {
        Thread.sleep(30);
    }

    /**
     * Start the observer.
     * <p>
     * This will start pumping events to the supplied {@link #progressConsumer}
     */
    public void start() {
        if (task.get() != null) throw new IllegalStateException("Already running");
        task.set(executor.submit(() -> {
            try {
                var size = Files.size(from);
                progressConsumer.accept(0D);
                while (!Thread.interrupted()) {
                    waitABit();
                    while (Files.notExists(to)) {
                        Thread.onSpinWait();
                    }
                    var toSize = Files.size(to);
                    if (size == toSize) {
                        progressConsumer.accept(100D);
                        break;
                    } else {
                        progressConsumer.accept(1D * toSize / size);
                    }
                }
            } catch (IOException ignore) {
                progressConsumer.accept(100D);
            }
        }));
    }

    /**
     * Stops this observer
     */
    public void stop() {
        var running = task.get();
        if (running != null) {
            running.cancel(true);
        }
        executor.shutdownNow();
    }

}
