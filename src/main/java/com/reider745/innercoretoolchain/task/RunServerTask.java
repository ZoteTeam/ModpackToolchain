package com.reider745.innercoretoolchain.task;

import com.reider745.innercoretoolchain.util.DownloadUtil;
import com.reider745.innercoretoolchain.util.Logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RunServerTask extends Task {
    private final File working;
    private final String url;

    public RunServerTask(File working, String url) {
        this.working = working;
        this.url = url;
    }

    @Override
    public void run() {
        final File file = new File(working, "server.jar");
        // Установка ядра, если отстувует

        if(!file.exists()) {
            Logs.message("Download server.jar, from: " + url);
            try {
                Files.write(Path.of(file.getPath()), DownloadUtil.readHttp(url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println();

        final ProcessBuilder pb = new ProcessBuilder("java", "-jar", file.getName(), "-Djava.net.preferIPv4Stack=true");
        pb.directory(working);
        pb.inheritIO();

        try {
            final Process process = pb.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }));

            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
