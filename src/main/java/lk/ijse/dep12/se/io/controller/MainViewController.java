package lk.ijse.dep12.se.io.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import lk.ijse.dep12.se.io.MyCopyTask;

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

        MyCopyTask myCopyTask = new MyCopyTask();
        Thread copyThread = new Thread(myCopyTask);
        copyThread.start();

        copyFiles(source, target, myCopyTask/*, copyThread*/);

        myCopyTask.progressProperty().addListener((observable, previous, current) -> {
            double progress = current.doubleValue() * 100;
            lblProgress.setText("%.2f".formatted(progress) + "%");
        });

        lblStatus.textProperty().bind(myCopyTask.messageProperty());
        pgbCopy.progressProperty().bind(myCopyTask.progressProperty());

        myCopyTask.setOnSucceeded(workerStateEvent -> {
            myCopyTask.updateMessage("Copied all files successfully. Reset to start again!");
            myCopyTask.updateProgress(100, 100);
        });

    }

    public void btnResetOnAction(ActionEvent actionEvent) {
        reset();
    }


    private void copyFiles(Path source, Path target, MyCopyTask myCopyTask/*, Thread copyThread*/) throws IOException {

        if (Files.notExists(target)) {
            myCopyTask.updateMessage("Copying " + source.getFileName());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); // copy the folder ( empty )
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }
        for (Path path : Files.newDirectoryStream(source)) {
            if (Files.notExists(target.resolve(path.getFileName()))) { //check whether this folder is already copied
                if (Files.isDirectory(path)) {
                    //if this is a directory, going inside it until only files are available
                    copyFiles(path, target.resolve(path.getFileName()), myCopyTask/*, copyThread*/);
                } else {
                    //copy items which are not directories
                    copiedSize += Files.size(path);
                    myCopyTask.updateMessage("Copying " + path.getFileName());
                    myCopyTask.updateProgress((double) copiedSize / totalSize * 100, 100);
                    Files.copy(path, target.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING); // copy everything into the target folder
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
                }
            }
        }

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
