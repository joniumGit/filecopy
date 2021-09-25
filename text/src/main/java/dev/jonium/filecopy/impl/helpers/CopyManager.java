package dev.jonium.filecopy.impl.helpers;

import dev.jonium.filecopy.CopyOperation;
import dev.jonium.filecopy.impl.CopyOperationImpl;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class CopyManager implements AutoCloseable {

    Path from;
    Path to;
    ExecutorService readThread;
    ExecutorService writeThread;

    public CopyManager(Path from, Path to) {
        this.from = from;
        this.to = to;
        readThread = Executors.newSingleThreadExecutor();
        writeThread = Executors.newSingleThreadExecutor();
    }

    public CopyOperation doCopy(Integer queueSize) {
        var queue = new LinkedBlockingQueue<Integer>(queueSize == null ? Integer.MAX_VALUE : queueSize);
        var startFlag = new CountDownLatch(1);
        var readTask = new ReadAction(startFlag, from, queue);
        var writeTask = new WriteAction(startFlag, to, queue);
        var reader = readThread.submit(readTask);
        var writer = writeThread.submit(writeTask);
        return new CopyOperationImpl(startFlag, reader, writer);
    }

    @Override
    public void close() {
        readThread.shutdown();
        writeThread.shutdown();
    }

}
