package dev.jonium.filecopy.impl.helpers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
abstract class ActionBase {
    CountDownLatch startFlag;
    Path operatingPath;
    BlockingQueue<Integer> queue;
}
