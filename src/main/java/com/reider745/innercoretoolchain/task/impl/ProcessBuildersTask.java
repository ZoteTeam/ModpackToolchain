package com.reider745.innercoretoolchain.task.impl;

import com.reider745.innercoretoolchain.task.Task;

import java.io.IOException;

public class ProcessBuildersTask extends Task {
    private ProcessBuilder[] processBuilders;

    public ProcessBuildersTask(ProcessBuilder... processBuilders) {
        this.processBuilders = processBuilders;
    }

    @Override
    public void run() {
        for (ProcessBuilder processBuilder : processBuilders) {
            try {
                processBuilder.start().waitFor();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
