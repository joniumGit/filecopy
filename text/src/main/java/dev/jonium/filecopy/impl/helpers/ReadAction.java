package dev.jonium.filecopy.impl.helpers;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class ReadAction extends ActionBase implements Runnable {

    public ReadAction(@NonNull CountDownLatch startFlag, @NonNull Path operatingPath, @NonNull BlockingQueue<Integer> queue) {
        super(startFlag, operatingPath, queue);
    }

    @SneakyThrows({InterruptedException.class, IOException.class})
    @Override
    public void run() {
        startFlag.await();
        try (var io = Files.newBufferedReader(operatingPath, StandardCharsets.UTF_8)) {
            while (!Thread.interrupted()) {
                var c = io.read();
                if (!queue.offer(c, 1, TimeUnit.SECONDS)) {
                    throw new IOException("Failed to transfer data)");
                } else if (c == -1) {
                    break;
                }
            }
        }
    }
}
