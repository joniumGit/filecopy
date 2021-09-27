package dev.jonium.filecopy.impl.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

class TestHelpers {

    private void withTemp(String s, ThrowingConsumer<Path> action) {
        Assertions.assertDoesNotThrow(() -> {
            var path = Files.createTempFile(s, "tmp");
            try {
                action.accept(path);
            } finally {
                Files.delete(path);
            }
        });
    }

    @Test
    void readerTestQueue() {
        var queue = new LinkedBlockingQueue<Integer>(10);
        withTemp("testQueue", path -> {
            Files.write(path, "123456789".getBytes(StandardCharsets.UTF_8));
            var latch = new CountDownLatch(1);
            latch.countDown();
            var rop = new ReadAction(latch, path, queue);
            rop.run();
        });
        Assertions.assertEquals(10, queue.size());
        // Removed eof
        var s = queue.stream().limit(9).mapToInt(i -> i).mapToObj(i -> (char) i).map(String::valueOf).collect(Collectors.joining());
        Assertions.assertEquals("123456789", s);
    }

    @Test
    void readerTestTimeout() {
        var queue = new LinkedBlockingQueue<Integer>(1);
        queue.add(1);
        withTemp("testTimeout", path -> {
            Files.write(path, "1".getBytes(StandardCharsets.UTF_8));
            var latch = new CountDownLatch(1);
            latch.countDown();
            var rop = new ReadAction(latch, path, queue);
            Assertions.assertThrows(IOException.class, rop::run);
        });
    }

    @Test
    void writerTestTimeout() {
        var queue = new LinkedBlockingQueue<Integer>(1);
        withTemp("writerTimeout", path -> {
            var latch = new CountDownLatch(1);
            latch.countDown();
            var wop = new WriteAction(latch, path, queue);
            Assertions.assertThrows(IOException.class, wop::run);
        });
    }


}
