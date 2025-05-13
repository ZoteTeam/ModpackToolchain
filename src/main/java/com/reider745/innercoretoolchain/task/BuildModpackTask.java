package com.reider745.innercoretoolchain.task;

import com.reider745.innercoretoolchain.make.ModpackMake;
import com.reider745.innercoretoolchain.type.Side;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class BuildModpackTask extends Task {
    private final Side side;
    private final File output;
    private final ModpackMake make;

    public BuildModpackTask(Side side, File working, File output, File makeFile) throws IOException {
        this.side = side;
        this.output = output;
        this.make = new ModpackMake(working, makeFile);
    }

    @Override
    public void run() {
        try {
            this.make.build(side, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
