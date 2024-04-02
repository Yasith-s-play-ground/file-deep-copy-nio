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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MainViewController {
    public TextField txtSource;
    public Button btnSourceDirectory;
    public TextField txtTarget;
    public Button btnTargetDirectory;
    public Button btnCopy;
    public Button btnReset;
    public Label lblStatus;
    public Label lblProgress;
    public ProgressBar pgbCopy;
    private long totalSize;
    private long copiedSize = 0;
    // private Task copyTask = null;
    private Thread fileCopyThread = null;

    public void initialize() {
        reset();
    }

    public void btnSourceDirectoryOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Folder");
        File file = directoryChooser.showDialog(btnSourceDirectory.getScene().getWindow());
        if (file == null) {
            txtSource.setText("No Folder Selected");
        } else {
            txtSource.setText(file.getAbsolutePath());
        }
        enableCopyButton();
    }

    public void btnTargetDirectoryOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Target Folder");
        File file = directoryChooser.showDialog(btnTargetDirectory.getScene().getWindow());
        if (file == null) {
            txtTarget.setText("No Folder Selected");
        } else {
            txtTarget.setText(file.getAbsolutePath());
        }
        enableCopyButton();
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {
        Path source = Paths.get(txtSource.getText());
        Path target = Paths.get(txtTarget.getText());
        totalSize = Files.size(source);
        copyFiles(source, target);

//        copyTask = new Task<>() {
//            @Override
//            protected Object call() throws Exception {
//                return null;
//            }
//        };
//        copyTask.progressProperty().addListener((observable, previous, current) -> {
//            double progress = current.doubleValue() * 100;
//            lblProgress.setText("%.2f".formatted(progress) + "%");
//        });
//        lblStatus.textProperty().bind(copyTask.messageProperty());
//        pgbCopy.progressProperty().bind(copyTask.progressProperty());
//
//        new Thread(copyTask).start();

        //lblStatus.setText("Copying complete !!");
//                            updateMessage("Copying " + path.getFileName());
//                            updateProgress((double) copiedSize / totalSize * 100, 100);
        // pgbCopy.progressProperty().setValue(1);
        // lblProgress.setText("100.00%");
    }

    public void btnResetOnAction(ActionEvent actionEvent) {
        reset();
    }


    private void copyFiles(Path source, Path target) throws IOException {
        Task timeConsumingTask = new Task<>() {
            @Override
            protected Object call() throws Exception {
                if (Files.notExists(target)) {
                    //lblStatus.setText("Copying " + source.getFileName());
                    updateMessage("Copying " + source.getFileName());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); // copy the folder ( empty )
                }
                for (Path path : Files.newDirectoryStream(source)) {
                    if (Files.notExists(target.resolve(path.getFileName()))) { //check whether this folder is already copied
                        if (Files.isDirectory(path)) {
                            //if this is a directory, going inside it until only files are available
                            copyFiles(path, target.resolve(path.getFileName()));
                        } else {
                            //copy items which are not directories
                            copiedSize += Files.size(path);
                            //lblStatus.setText("Copying " + path.getFileName());
                            updateMessage("Copying " + path.getFileName());
                            updateProgress((double) copiedSize / totalSize * 100, 100);
                            //pgbCopy.progressProperty().setValue((double) copiedSize / totalSize);
                            //lblProgress.setText("%.2f".formatted((double) copiedSize / totalSize) + "%");
                            Files.copy(path, target.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING); // copy everything into the target folder
                        }
                    }
                }
                updateMessage("Installation completed. Click to reset again!");
                return null;
            }
        };
        timeConsumingTask.progressProperty().addListener((observable, previous, current) -> {
            double progress = current.doubleValue() * 100;
            lblProgress.setText("%.2f".formatted(progress) + "%");
        });
        lblStatus.textProperty().bind(timeConsumingTask.messageProperty());
        pgbCopy.progressProperty().bind(timeConsumingTask.progressProperty());

        //if thread is not initialized, create and start new thread
//        if (fileCopyThread == null) {
//            System.out.println("thread is not initialized");
//            fileCopyThread = new Thread(timeConsumingTask);
//            fileCopyThread.start();
//        }

        new Thread(timeConsumingTask).start();

    }

    private void enableCopyButton() {
        btnCopy.setDisable(txtSource.getText().isEmpty() || txtTarget.getText().isEmpty());
    }

    private void reset() {
        txtTarget.clear();
        txtSource.clear();
        enableCopyButton();
        pgbCopy.progressProperty().setValue(0);
        lblStatus.setText("");
        lblProgress.setText("");
    }
}
