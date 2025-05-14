package com.reider745.innercoretoolchain.make;

import com.google.gson.Gson;
import com.reider745.innercoretoolchain.json.modpack.ServerDescriptionJson;
import com.reider745.innercoretoolchain.util.Logs;
import com.reider745.innercoretoolchain.util.icmods.IcmodMod;
import com.reider745.innercoretoolchain.json.mod.ModMakeJson;
import com.reider745.innercoretoolchain.json.modpack.DescriptionModJson;
import com.reider745.innercoretoolchain.json.modpack.ModpackMakeJson;
import com.reider745.innercoretoolchain.make.mod.AssemlyMod;
import com.reider745.innercoretoolchain.make.mod.DownloadMod;
import com.reider745.innercoretoolchain.make.mod.Mod;
import com.reider745.innercoretoolchain.type.Side;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class ModpackMake {
    private static final Gson GSON = new Gson();

    private final File work;
    private final File output;
    private final File mods;
    private final ModpackMakeJson make;

    public ModpackMake(File work, File makeFile) throws IOException {
        work.mkdirs();
        this.work = work;
        this.make = GSON.fromJson(Files.readString(makeFile.toPath()), ModpackMakeJson.class);

        this.output = new File(work, "output");
        this.mods = new File(work, "mods.json");
    }

    private void initDirectories() {
        output.mkdirs();
        mods.mkdirs();
    }

    private Mod getMod(DescriptionModJson mod) throws IOException {
        if(mod.path != null) {
            final File file = new File(work, mod.path);
            return new AssemlyMod(file, GSON.fromJson(Files.readString(new File(file, "make.json").toPath()), ModMakeJson.class));
        }
        return new DownloadMod(new IcmodMod(make.proxy, mod.modId));
    }

    public List<Mod> getMods(Side side) throws IOException {
        final List<Mod> mods = new ArrayList<>();

        if(make.mods != null) {
            for (DescriptionModJson mod : make.mods) {
                mods.add(getMod(mod));
            }
        }

        if(side == Side.CLIENT && make.client != null) {
            for(DescriptionModJson mod : make.client.mods) {
                mods.add(getMod(mod));
            }
        }

        if(side == Side.SERVER && make.server != null) {
            for(DescriptionModJson mod : make.server.mods) {
                mods.add(getMod(mod));
            }
        }

        return mods;
    }

    public File getOutput(Side side) {
        return new File(output, side.name().toLowerCase());
    }

    public void build(Side side, File directory) throws IOException {
        if(directory == null)
            directory = getOutput(side);
        directory.mkdirs();

        Files.writeString(new File(directory, "modpack.json").toPath(), GSON.toJson(make.buildManifest(side)));

        final File modsFile = new File(directory, "mods");
        modsFile.mkdirs();

        final File configFile = new File(directory, "config");
        configFile.mkdirs();

        final List<Mod> mods = getMods(side);
        for(Mod mod : mods) {
            final File outputMod = new File(modsFile, mod.getName());
            outputMod.mkdirs();
            mod.build(outputMod, new File(configFile, mod.getName() + "-config.json"));
        }

        {
            final StringBuilder builder = new StringBuilder();
            final ServerDescriptionJson[] servers = this.make.get(side, "servers", new ServerDescriptionJson[0]);

            int index = 1;
            for(ServerDescriptionJson server : servers) {
                builder.append(index++);
                builder.append(':');
                builder.append(server.name);
                builder.append(':');
                builder.append(server.ip);
                builder.append(':');
                builder.append(server.port);
                builder.append(':');
                builder.append(index);
                builder.append('\n');
            }

            Files.writeString(
                    new File(directory, "external_servers.txt").toPath(),
                    builder.toString()
            );
        }
    }

    public void build(Side side) throws IOException {
        build(side, null);
    }
}
