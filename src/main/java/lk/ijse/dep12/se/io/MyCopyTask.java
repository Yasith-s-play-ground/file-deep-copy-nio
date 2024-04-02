package lk.ijse.dep12.se.io;

import javafx.concurrent.Task;

public class MyCopyTask extends Task {
    @Override
    protected Object call() throws Exception {
        return null;
    }

    @Override
    public void updateProgress(double v, double v1) {
        super.updateProgress(v, v1);
    }

    @Override
    public void updateMessage(String s) {
        super.updateMessage(s);
    }
}
