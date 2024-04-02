package lk.ijse.dep12.se.io.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class MainViewFileWalkerController {

    public Button btnCopy;

    public Button btnReset;

    public Button btnSourceDirectory;

    public Button btnTargetDirectory;

    public Label lblProgress;

    public Label lblStatus;

    public ProgressBar pgbCopy;

    public TextField txtSource;

    public TextField txtTarget;

    private double totalSize;

    // private Path target;

//    private Path getTargetFolderName(Path source, Path target) {
//        if (Files.exists(target.resolve(source.getFileName() + "(copy)"))) {
//            target = getTargetFolderName(Path.of(source.getFileName() + "(copy)"), target);
//        }
//        return target;
//    }

//    private void setTarget(Path newTarget) {
//        target = newTarget;
//    }

    public void btnCopyOnAction(ActionEvent event) throws IOException {
        double completedSize = 0;
        String source = txtSource.getText();
        String target = txtTarget.getText();
        if (source.isBlank()) {
            txtSource.requestFocus();
            return;
        }
        if (target.isBlank()) {
            txtTarget.requestFocus();
            return;
        }
        Task<Object> copyTask = new Task<>() {
            @Override
            protected Object call() throws Exception {
                updateMessage("Analyzing the folder content");
                Path sourcePath = Paths.get(txtSource.getText());
                Path targetPath = Paths.get(txtTarget.getText(), sourcePath.getFileName() + "");
                while (Files.exists(targetPath)){
                    targetPath = Paths.get(targetPath + " (copy)");
                }

                ArrayList<Path> paths = new ArrayList<>();
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!sourcePath.equals(dir)) paths.add(dir);
                        return super.preVisitDirectory(dir, attrs);
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        paths.add(file);
                        return super.visitFile(file, attrs);
                    }
                });
                if (Files.notExists(targetPath)) {
                    updateMessage("Create target directory");
                    Files.createDirectory(targetPath);
                }
                int i = 0;
                for (Path path : paths) {
                    if (Files.isDirectory(path)) {
                        updateMessage("Creating directory: " + path.getFileName());
                    } else {
                        updateMessage("Copying file: " + path.getFileName());
                    }
                    Files.copy(path, targetPath.resolve(sourcePath.relativize(path)));
                    updateProgress(++i, paths.size());
                    Thread.sleep(100);
                }
                updateMessage("Copied Successfully");
                return null;
            }
        };

        copyTask.progressProperty().addListener((observable, previous, current) -> {
            lblProgress.setText((int) (current.doubleValue() * 100) + "%");
        });
        pgbCopy.progressProperty().bind(copyTask.progressProperty());
        lblStatus.textProperty().bind(copyTask.messageProperty());
        new Thread(copyTask).start();

    }

    public void btnResetOnAction(ActionEvent event) {
        reset();
    }

    public void btnSourceDirectoryOnAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Folder");
        File file = directoryChooser.showDialog(btnSourceDirectory.getScene().getWindow());
        if (file == null) {
            txtSource.setText("");
        } else {
            txtSource.setText(file.getAbsolutePath());
        }
        enableCopyButton();
    }

    public void btnTargetDirectoryOnAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Target Folder");
        File file = directoryChooser.showDialog(btnTargetDirectory.getScene().getWindow());
        if (file == null) {
            txtTarget.setText("");
        } else {
            txtTarget.setText(file.getAbsolutePath());
        }
        enableCopyButton();
    }

    private void enableCopyButton() {
        btnCopy.setDisable(txtSource.getText().isEmpty() || txtTarget.getText().isEmpty());
    }

    private void reset() {
        txtTarget.clear();
        txtSource.clear();
        enableCopyButton();
        lblStatus.textProperty().unbind();
        pgbCopy.progressProperty().unbind();
        pgbCopy.progressProperty().setValue(0);
        lblStatus.setText("");
        lblProgress.setText("");

    }
}
