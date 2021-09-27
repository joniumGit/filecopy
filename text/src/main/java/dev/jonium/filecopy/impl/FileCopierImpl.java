package dev.jonium.filecopy.impl;

import dev.jonium.filecopy.CopyOperation;
import dev.jonium.filecopy.impl.helpers.CopyManager;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple Text File Copier
 */
public final class FileCopierImpl implements dev.jonium.filecopy.FileCopier {

    @Setter(onMethod_ = {@Override})
    private Integer bufferSize;

    /**
     * Checks that the file exists and is readable
     *
     * @param file Any path
     */
    private void checkExists(Path file) {
        if (Files.notExists(file) || !Files.isReadable(file)) {
            throw new IllegalArgumentException("Source doesn't exists");
        }
    }

    /**
     * Checks that the specified files are not the same
     * <p>
     * If b doesn't exist the files are assumed not same.
     * Otherwise, both are expected to exist and are compared
     *
     * @param a Path
     * @param b Path
     */
    private void checkNotSame(Path a, Path b) {
        try {
            if (Files.exists(b) && Files.isSameFile(a, b)) {
                throw new IllegalArgumentException("Source and Destination are the same file");
            }
        } catch (IOException e) {
            // Shouldn't happen
            throw new IllegalArgumentException("Failed identity check", e);
        }
    }

    /**
     * Checks that the two paths are not directories
     *
     * @param a Path
     * @param b Path
     */
    private void checkNotDirectories(Path a, Path b) {
        if (Files.isDirectory(a) || Files.exists(b) && Files.isDirectory(b)) {
            throw new IllegalArgumentException("Directory is not a valid argument");
        }
    }

    /**
     * If a file exists, checks is it writable
     *
     * @param file Path
     */
    private void checkWritable(Path file) {
        if (Files.exists(file) && !Files.isWritable(file)) {
            throw new IllegalArgumentException("Destination is not writable");
        }
    }

    /**
     * Copies a <b>Text (UTF-8)</b> files from A to B
     * Throws {@link IllegalArgumentException} on bad arguments
     *
     * @param from {@link Path} to copy from
     * @param to   {@link Path} to copy to
     * @return Result of the operation
     */
    public CopyOperation copy(@NonNull Path from, @NonNull Path to) {
        checkExists(from);
        checkNotDirectories(from, to);
        checkNotSame(from, to);
        checkWritable(to);
        try (var cm = new CopyManager(from, to)) {
            return cm.doCopy(bufferSize);
        }
    }

}
