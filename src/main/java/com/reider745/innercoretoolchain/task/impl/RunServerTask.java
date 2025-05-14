package com.reider745.innercoretoolchain.task.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.reider745.innercoretoolchain.task.Task;
import com.reider745.innercoretoolchain.util.DownloadUtil;
import com.reider745.innercoretoolchain.util.Logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

        try {
            final File zotecoreFile = new File(working, "zotecore.yml");
            if(!zotecoreFile.exists()) {
                zotecoreFile.createNewFile();

                final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
                final Map<String, String> yaml = new HashMap<>();

                yaml.put("pack-version-code", "any");
                yaml.put("modpack", "dev");

                mapper.writeValue(zotecoreFile, yaml);
            }

            System.out.println();


            final ProcessBuilder pb = new ProcessBuilder("java", "-jar", file.getName(), "-Djava.net.preferIPv4Stack=true");
            pb.directory(working);
            pb.inheritIO();

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
