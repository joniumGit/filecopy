package dev.jonium.filecopy.impl.helpers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Abstract base for reader and writer
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
abstract class ActionBase {
    /**
     * Flag to wait on for operation start
     */
    @NonNull CountDownLatch startFlag;
    /**
     * Current operating path
     */
    @NonNull Path operatingPath;
    /**
     * Memory buffer
     */
    @NonNull BlockingQueue<Integer> queue;
}
