package dev.jonium.filecopy.ui;


import dev.jonium.filecopy.CopyOperation;
import dev.jonium.filecopy.FileCopier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class Copier extends Application {

    Image image;
    FileCopier copyService;
    MainView view;
    final AtomicReference<CopyOperation> currentTask = new AtomicReference<>();

    private Alert newAlert(String message) {
        var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        try {
            var p = alert.getDialogPane().getScene().getWindow();
            var icons = p.getClass().getMethod("getIcons").invoke(p);
            icons.getClass().getMethod("add", Object.class).invoke(icons, image);
        } catch (Exception ignore) {
            // This is not important
        }
        alert.initModality(Modality.APPLICATION_MODAL);
        return alert;
    }

    private void showError(Exception e) {
        var alert = newAlert("Operation failed:\n" + e.getMessage());
        alert.setTitle("Error");
        alert.showAndWait();
        currentTask.set(null);
        view.stopProgress();
    }

    private void showSuccess(long time) {
        var alert = newAlert("Operation successful");
        var timeStr = time > 1000 ? time / 1000 + "s" : time + "ms";
        alert.setTitle("Success (" + timeStr + ")");
        alert.showAndWait();
        currentTask.set(null);
        view.stopProgress();
    }

    private void copy(Path from, Path to) {
        try {
            var operation = copyService.copy(from, to);
            var observer = new ProgressObserver(from, to, d -> Platform.runLater(() -> view.setProgress(d)));
            operation.onSuccess(l -> CompletableFuture.runAsync(() -> {
                view.setProgress(100D);
                observer.stop();
                showSuccess(l);
            }, Platform::runLater));
            operation.onFailure(ex -> CompletableFuture.runAsync(() -> {
                observer.stop();
                showError(ex);
            }, Platform::runLater));
            operation.onCancel(() -> CompletableFuture.runAsync(observer::stop, Platform::runLater));
            view.startProgress();
            currentTask.set(operation);
            observer.start();
            operation.start();
        } catch (IllegalArgumentException ex) {
            showError(ex);
        }
    }

    private void cancel() {
        var task = currentTask.getAndSet(null);
        if (task != null) {
            task.cancel();
            view.stopProgress();
        }
    }

    private void initIcon() {
        try (var io = Copier.class.getResourceAsStream("icon.png")) {
            // Should never throw
            image = new Image(Objects.requireNonNull(io));
        } catch (IOException e) {
            image = new WritableImage(16, 16);
        }
    }

    private boolean initCopyService() {
        var impl = ServiceLoader.load(FileCopier.class).findFirst();
        if (impl.isPresent()) {
            copyService = impl.get();
            try {
                var params = getParameters().getNamed();
                if (params.containsKey("buffer-size")) {
                    var size = Integer.parseInt(params.get("buffer-size"));
                    Logger.getAnonymousLogger().info(() -> "Buffer size configured at: " + size);
                    copyService.setBufferSize(size);
                } else {
                    copyService.setBufferSize(2048);
                }
            } catch (NumberFormatException | NullPointerException e) {
                copyService.setBufferSize(2048);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start(Stage stage) {
        initIcon();
        stage.setTitle("Text File Copier");
        stage.getIcons().add(image);
        if (initCopyService()) {
            view = new MainView();
            view.setCopyAction(this::copy);
            view.setCancelAction(this::cancel);
            stage.setScene(new Scene(view));
            Platform.runLater(() -> {
                stage.setMinWidth(stage.getWidth());
                stage.setMinHeight(stage.getHeight());
            });
            stage.show();
        } else {
            var alert = newAlert("Failed to load copying service");
            alert.setTitle("Error in Service Loader");
            alert.show();
        }
    }

}
