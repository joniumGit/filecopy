package dev.jonium.filecopy.impl.helpers;

import dev.jonium.filecopy.impl.FileCopierImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

class TestCopy {

    /**
     * Test the full copying process
     */
    @Test
    void testCopy() {
        Assertions.assertDoesNotThrow(() -> {
            var p1 = Files.createTempFile("copyTest1", "tmp");
            var p2 = Files.createTempFile("copyTest2", "tmp");
            Files.write(p1, "abcd".getBytes(StandardCharsets.UTF_8));
            var latch = new CountDownLatch(1);
            var op = new FileCopierImpl().copy(p1, p2);
            op.onSuccess(l -> latch.countDown());
            op.onFailure(e -> {
                latch.countDown();
                Assertions.fail(e);
            });
            op.onCancel(() -> {
                latch.countDown();
                Assertions.fail();
            });
            op.start();
            latch.await();
            Assertions.assertEquals("abcd", Files.readString(p2));
        });
    }

    /**
     * Test the full copying process
     */
    @Test
    void testCopyCancel() {
        Assertions.assertDoesNotThrow(() -> {
            var p1 = Files.createTempFile("copyTest3", "tmp");
            var p2 = Files.createTempFile("copyTest4", "tmp");
            Files.write(p1, "abcd".getBytes(StandardCharsets.UTF_8));
            var latch = new CountDownLatch(1);
            var op = new FileCopierImpl().copy(p1, p2);
            op.onSuccess(l -> {
                latch.countDown();
                Assertions.fail();
            });
            op.onFailure(e -> {
                latch.countDown();
                Assertions.fail(e);
            });
            op.onCancel(latch::countDown);
            op.start();
            // Should be ok
            op.cancel();
            latch.await();
        });
    }

    /**
     * Try to induce error in copying.
     * <br>
     * This should make the writer throw an IOException inside the executor,
     * and it should be handled gracefully
     */
    @Test
    void testCopyFail() {
        Assertions.assertDoesNotThrow(() -> {
            var p1 = Files.createTempFile("copyTest1", "tmp");
            var p2 = Files.createTempFile("copyTest2", "tmp");
            Assertions.assertTrue(p2.toFile().setWritable(false));
            Files.write(p1, "abcd".getBytes(StandardCharsets.UTF_8));
            var latch = new CountDownLatch(1);
            try (var mngr = new CopyManager(p1, p2)) {
                var op = mngr.doCopy(1);
                op.onSuccess(l -> {
                    latch.countDown();
                    Assertions.fail();
                });
                op.onFailure(e -> {
                    latch.countDown();
                    Assertions.assertEquals(IOException.class, e.getClass());
                });
                op.onCancel(() -> {
                    latch.countDown();
                    Assertions.fail();
                });
                op.start();
            }
            latch.await();
        });
    }

}
