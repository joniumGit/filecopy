package dev.jonium.filecopy;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * This interface represents the copying operation
 */
@SuppressWarnings("unused")
public interface CopyOperation {

    /**
     * Callback on successful copy
     *
     * @param onSuccess Consumer for time in {@link java.util.concurrent.TimeUnit#MILLISECONDS}
     */
    void onSuccess(LongConsumer onSuccess);

    /**
     * Callback on a failed copy
     *
     * @param onFailure Consumer for time in {@link java.util.concurrent.TimeUnit#MILLISECONDS} and the failure reason if available
     */
    void onFailure(Consumer<Exception> onFailure);

    /**
     * Callback on cancelled copy
     *
     * @param onCancel Consumer for time in {@link java.util.concurrent.TimeUnit#MILLISECONDS}
     */
    void onCancel(Runnable onCancel);

    /**
     * Cancel this operation if possible
     */
    void cancel();

    /**
     * Start the copy operation
     */
    void start();

}
