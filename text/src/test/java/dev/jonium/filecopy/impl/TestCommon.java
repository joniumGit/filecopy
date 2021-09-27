package dev.jonium.filecopy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


/**
 * Tests the inputs for the file copier
 */
@DisplayName("Common Tests")
class TestCommon {

    private final FileCopierImpl copier = new FileCopierImpl();

    private Path getExample() {
        return Assertions.assertDoesNotThrow(() -> Path.of(Objects.requireNonNull(getClass().getResource("/testfile")).toURI()));
    }

    @Test
    void testFilesNotNull() {
        var p = getExample();
        Assertions.assertThrows(NullPointerException.class, () -> copier.copy(null, p));
        Assertions.assertThrows(NullPointerException.class, () -> copier.copy(p, null));
        Assertions.assertThrows(NullPointerException.class, () -> copier.copy(null, null));
    }

    @Test
    void testNoSameFile() {
        var p = getExample();
        Assertions.assertThrows(IllegalArgumentException.class, () -> copier.copy(p, p));
    }

    @Test
    void testNoDirectory() {
        var p1 = Path.of("/");
        var p2 = getExample();
        Assertions.assertThrows(IllegalArgumentException.class, () -> copier.copy(p1, p2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> copier.copy(p2, p1));
    }

    @Test
    void testFileExists() {
        var existing = getExample();
        var notExisting1 = Path.of("/output1");
        Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> Files.notExists(notExisting1)));
        var notExisting2 = Path.of("/output2");
        Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> Files.notExists(notExisting2)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> copier.copy(notExisting1, notExisting2));
        Assertions.assertDoesNotThrow(() -> copier.copy(existing, notExisting2));
    }

    @Test
    void testNotWritable() {
        var existing = getExample();
        var p = Assertions.assertDoesNotThrow(() -> Files.createTempFile("notWritable", "tmp"));
        Assertions.assertTrue(p.toFile().setWritable(false));
        Assertions.assertThrows(IllegalArgumentException.class, () -> copier.copy(existing, p));
    }

}
