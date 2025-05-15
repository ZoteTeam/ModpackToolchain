package com.reider745.innercoretoolchain.task.impl;

import com.reider745.innercoretoolchain.Toolchain;
import com.reider745.innercoretoolchain.task.Task;
import com.reider745.innercoretoolchain.util.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UpdateClasspathTask extends Task {
    @Override
    public void run() {
        final String[] files = new String[] {
                "android.jar",
                "horizon-1.2.jar",
                "innercore-test.jar"
        };

        for(String file : files) {
            final File outputJava = new File(Toolchain.getCLASSPATH(), file);
            if (!outputJava.exists()) {
                try {
                    Files.write(outputJava.toPath(), DownloadUtil.readHttp("https://raw.githubusercontent.com/zheka2304/innercore-mod-toolchain/refs/heads/develop/toolchain/toolchain/classpath/" + file));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
