package com.reider745.innercoretoolchain.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Task implements Runnable {
    protected Task beforeTask, afterTask;

    public final void execute() {
        if (beforeTask != null) {
            beforeTask.execute();
        }

        run();

        if (afterTask != null) {
            afterTask.execute();
        }
    }
}
