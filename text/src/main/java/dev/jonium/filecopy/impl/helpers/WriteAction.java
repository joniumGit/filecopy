package dev.jonium.filecopy.impl.helpers;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class WriteAction extends ActionBase implements Runnable {

    public WriteAction(CountDownLatch startFlag, Path operatingPath, BlockingQueue<Integer> queue) {
        super(startFlag, operatingPath, queue);
    }

    @SneakyThrows({InterruptedException.class, IOException.class})
    @Override
    public void run() {
        startFlag.await();
        try (var io = Files.newBufferedWriter(operatingPath, StandardCharsets.UTF_8)) {
            while ( !Thread.interrupted() ) {
                var c = queue.poll(1, TimeUnit.SECONDS);
                if (c != null) {
                    if (c == -1) {
                        break;
                    } else {
                        io.write(c);
                    }
                } else {
                    throw new IOException("Failed to get more data");
                }
            }
        }
    }

}
