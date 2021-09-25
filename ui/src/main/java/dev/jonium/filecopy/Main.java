package dev.jonium.filecopy;

import dev.jonium.filecopy.ui.Copier;
import javafx.application.Application;
import lombok.SneakyThrows;

/**
 * Launcher
 */
public final class Main {

    /**
     * Sometimes launching JavaFX apps directly causes some issues with imports and stuff,
     * and I have found that this workaround works quite well.
     *
     * @param args Program arguments
     */
    @SneakyThrows
    public static void main(String[] args) {
        Application.launch(Copier.class, args);
    }

}
