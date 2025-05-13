package com.reider745.innercoretoolchain.make.mod;

import com.reider745.innercoretoolchain.util.Logs;
import com.reider745.innercoretoolchain.util.icmods.IcmodMod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadMod extends Mod {
    private final IcmodMod mod;

    public DownloadMod(IcmodMod mod) {
        super(mod.getName());
        this.mod = mod;
    }

    private void download(File destinationDir, File configOutput) {
        try {
            Logs.process("Download: " + getName(), "Finished download: " + getName(), () -> {
                try {
                    final byte[] bytes = mod.download();
                    ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(bytes));

                    ZipEntry zipEntry;
                    String removedDir = "";
                    while ((zipEntry = stream.getNextEntry()) != null) {
                        if(zipEntry.getName().endsWith("/build.config")) {
                            removedDir = zipEntry.getName().replace("/build.config", "");
                            break;
                        }
                    }

                    stream = new ZipInputStream(new ByteArrayInputStream(bytes));

                    while ((zipEntry = stream.getNextEntry()) != null) {
                        File entryFile = new File(destinationDir, zipEntry.getName().replace(removedDir, ""));

                        if (zipEntry.isDirectory()) {
                            entryFile.mkdirs();
                        } else {
                            final File parentDir = entryFile.getParentFile();
                            if (parentDir != null && !parentDir.exists()) {
                                parentDir.mkdirs();
                            }

                            if(zipEntry.getName().endsWith("config.json")) {
                                Files.write(configOutput.toPath(), stream.readNBytes((int) zipEntry.getSize()));
                            } else
                                Files.write(entryFile.toPath(), stream.readNBytes((int) zipEntry.getSize()));
                        }

                        stream.closeEntry();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build(File output, File configOutput) {
        final File cacheVersion = new File(output, ".toolchain_version");

        try {
            boolean lastVersion = cacheVersion.exists();
            if(lastVersion) {
                lastVersion = this.mod.getVersion() == Integer.parseInt(Files.readString(cacheVersion.toPath()));
            }
            if(!output.exists() || !lastVersion) {
                output.mkdirs();
                download(output, configOutput);
                Files.writeString(cacheVersion.toPath(), String.valueOf(this.mod.getVersion()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
