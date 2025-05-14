package com.reider745.innercoretoolchain.task.impl;

import com.reider745.innercoretoolchain.task.Task;

import java.io.IOException;

public class PushTask extends Task {
    private final String path;
    private final String pushTo;

    public PushTask(String path, String pushTo) {
        this.path = path;
        this.pushTo = pushTo;
    }

    @Override
    public void run() {
        if(path == null || pushTo == null) {
            return;
        }

        final ProcessBuilder pb = new ProcessBuilder("adb", "push", path, pushTo);
        pb.inheritIO();

        try {
            pb.start().waitFor();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
