package dev.jonium.filecopy.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Main file copier view
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
final class MainView extends BorderPane {

    TextField source;
    TextField destination;
    Button sourceSelect;
    Button destinationSelect;
    Button copy;
    Button cancel;
    ProgressBar progress;

    public MainView() {
        progress = new ProgressBar();
        source = new TextField();
        destination = new TextField();

        sourceSelect = new Button("Select");
        destinationSelect = new Button("Select");
        copy = new Button("Copy");
        cancel = new Button("Cancel");

        sourceSelect.setOnAction(e -> Optional.ofNullable(getFile(false)).ifPresent(source::setText));
        destinationSelect.setOnAction(e -> Optional.ofNullable(getFile(true)).ifPresent(destination::setText));

        source.setMinWidth(300);
        destination.setMinWidth(300);

        cancel.setCancelButton(true);
        copy.setDefaultButton(true);
        copy.setPrefSize(75, 25);
        cancel.setPrefSize(75, 25);
        sourceSelect.setPrefSize(75, 25);
        destinationSelect.setPrefSize(75, 25);
        progress.setPrefHeight(25);

        stopProgress();
        makeUI();
    }

    private String getFile(boolean save) {
        var fc = new FileChooser();
        fc.setTitle("Select a file");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File (txt)", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        var file = save
                ? fc.showSaveDialog(getScene().getWindow())
                : fc.showOpenDialog(getScene().getWindow());
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    private Node boxed(Node... nodes) {
        var box = new HBox();
        box.getChildren().addAll(nodes);
        box.setSpacing(5);
        return box;
    }

    private void makeUI() {
        setPadding(new Insets(5));
        var middle = new VBox();
        middle.getChildren().addAll(
                new Label("From"),
                boxed(source, sourceSelect),
                new Label("To"),
                boxed(destination, destinationSelect)
        );
        middle.setPadding(new Insets(0, 0, 10, 0));
        setCenter(middle);
        var bottom = new HBox();
        bottom.setPadding(new Insets(2, 0, 0, 0));
        bottom.setBorder(new Border(new BorderStroke(
                Paint.valueOf(Color.DARKGRAY.toString()),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(2, 0, 0, 0)
        )));
        bottom.getChildren().addAll(progress, copy, cancel);
        bottom.setSpacing(5);
        bottom.setAlignment(Pos.BOTTOM_RIGHT);
        setBottom(bottom);

        source.prefWidthProperty().bind(middle.widthProperty().add(sourceSelect.prefWidthProperty().negate()));
        destination.prefWidthProperty().bind(middle.widthProperty().add(destinationSelect.prefWidthProperty().negate()));
        progress.prefWidthProperty().bind(bottom.widthProperty().add(copy.prefWidthProperty().negate()).add(cancel.prefWidthProperty().negate()));
    }

    /**
     * Callback to run on user clicking Copy
     *
     * @param copyAction Runnable
     */
    void setCopyAction(BiConsumer<Path, Path> copyAction) {
        copy.setOnAction(e -> {
            var fromPath = Path.of(source.getText());
            var toPath = Path.of(destination.getText());
            copyAction.accept(fromPath, toPath);
        });
    }

    /**
     * Callback to run on user clicking Cancel
     *
     * @param cancelAction Runnable
     */
    void setCancelAction(Runnable cancelAction) {
        cancel.setOnAction(e -> cancelAction.run());
    }

    /**
     * Set ProgressBar progress
     *
     * @param percent progress in percent
     */
    void setProgress(double percent) {
        progress.setProgress(percent);
    }

    /**
     * Enable progress bar and set to busy state
     */
    void startProgress() {
        disableComponents();
        progress.setVisible(true);
        progress.setProgress(0D);
        cancel.setDisable(false);
    }

    /**
     * Disable busy bar and set to free state
     */
    void stopProgress() {
        progress.setVisible(false);
        progress.setProgress(0D);
        enableComponents();
        cancel.setDisable(true);
    }

    private void disableComponents() {
        for (var b : List.of(copy, sourceSelect, destinationSelect, source, destination)) {
            b.setDisable(true);
        }
    }

    private void enableComponents() {
        for (var b : List.of(copy, sourceSelect, destinationSelect, source, destination)) {
            b.setDisable(false);
        }
    }

}
