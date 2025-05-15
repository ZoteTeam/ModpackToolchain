package com.reider745.innercoretoolchain.task.impl;

import com.reider745.innercoretoolchain.Toolchain;
import com.reider745.innercoretoolchain.task.Task;
import com.reider745.innercoretoolchain.util.DownloadUtil;
import com.reider745.innercoretoolchain.util.Logs;

import java.io.File;
import java.nio.file.Files;

public class UpdateDeclarationsTask extends Task {
    @Override
    public void run() {
        final File declarations = Toolchain.getDeclarations();
        try {
            Files.write(new File(declarations, "declarations/launcher.d.ts").toPath(), Toolchain.class.getClassLoader().getResourceAsStream("declarations/launcher.d.ts").readAllBytes());
            Files.writeString(
                    new File(declarations, "core-engine.d.ts").toPath(),
                    DownloadUtil.readStringHttp(
                            "https://raw.githubusercontent.com/zheka2304/innercore-mod-toolchain/refs/heads/develop/toolchain/toolchain/declarations/core-engine.d.ts"
                    )
            );
            Files.writeString(
                    new File(declarations, "android.d.ts").toPath(),
                    DownloadUtil.readStringHttp(
                            "https://raw.githubusercontent.com/zheka2304/innercore-mod-toolchain/refs/heads/develop/toolchain/toolchain/declarations/android.d.ts"
                    )
            );

            Logs.message("Install declarations..");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
