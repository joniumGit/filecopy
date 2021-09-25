package dev.jonium.filecopy;

import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * This interface represents a file copying service
 */
@SuppressWarnings("unused")
public interface FileCopier {

    /**
     * Configures the internal buffer to be used in the copy operation.
     * This can vary between implementations, but should generally indicate how many units
     * the read-write operation can buffer in memory.
     *
     * @param size Size of the buffer, null indicates an implementation dependent default
     */
    void setBufferSize(Integer size);

    /**
     * Copy a file from location A to location B.
     * <br>
     * Both the source and destination have to be files.
     *
     * @param from {@link Path} to copy from
     * @param to   {@link Path} to copy to
     * @return {@link CopyOperation} of this operation
     * @see java.nio.file.Files#isDirectory(Path, LinkOption...)
     */
    CopyOperation copy(Path from, Path to);

}
