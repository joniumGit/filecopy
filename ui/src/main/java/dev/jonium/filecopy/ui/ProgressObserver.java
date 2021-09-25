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

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class ProgressObserver {

    Path from;
    Path to;
    Consumer<Double> progressConsumer;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicReference<Future<?>> task = new AtomicReference<>();

    @SneakyThrows(InterruptedException.class)
    private void waitABit() {
        Thread.sleep(30);
    }

    public void start() {
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

    public void stop() {
        var running = task.get();
        if (running != null) running.cancel(true);
        executor.shutdownNow();
    }

}
