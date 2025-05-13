package com.reider745.innercoretoolchain.make.mod;

import com.google.gson.Gson;
import com.reider745.innercoretoolchain.Main;
import com.reider745.innercoretoolchain.json.mod.*;
import com.reider745.innercoretoolchain.util.Logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AssemlyMod extends Mod {
    private static final Gson GSON = new Gson();
    private final ModMakeJson make;
    private final File dir;

    public AssemlyMod(File dir, ModMakeJson make) {
        super(make.info.name);
        this.dir = dir;
        this.make = make;
    }

    private static List<String> getDeclarations(String type) {
        final List<String> declarations = new ArrayList<>();
        if(type.equals("launcher"))
            declarations.add(Main.getDeclarationsPath() + "launcher.d.ts");

        declarations.add(Main.getDeclarationsPath() + "core-engine.d.ts");
        declarations.add(Main.getDeclarationsPath() + "android.d.ts");

        return declarations;
    }

    private void processSources(File output, String type, String fileName, List<File> sources, File directoryTsconfig, List<String> declarations) throws IOException {
        final File tsConfig = new File(directoryTsconfig, "tsconfig.json");
        final File outputFile = new File(output, fileName);
        outputFile.createNewFile();

        Files.writeString(tsConfig.toPath(), GSON.toJson(new TsConfigJson(
                outputFile.getAbsolutePath(),
                sources,
                declarations
        )));

        final ProcessBuilder builder = new ProcessBuilder("tsc", "-project", tsConfig.getAbsolutePath(), "--noEmitOnError");
        builder.inheritIO();

        try {
            builder.start().waitFor();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildSource(File output, SourceDescriptionJson source, BuildConfigJson buildConfigJson, List<String> declarations) throws IOException {
        final File file = new File(dir, source.source);

        if(!file.exists())
            return;


        if(source.type.equals("library")) {
            source.fast = true;
            source.name = new File(output, source.source).getName();
        }

        if(source.name == null)
            source.name = source.type + ".js";

        if(source.fast && file.isFile()) {
            if(clone(new File(this.dir, source.source), new File(output, source.name)))
                buildConfigJson.compile.add(new BuildConfigJson.CompileSourceJson(source.name, source.type));
            return;
        }

        if(file.isDirectory()) {
            final File includes = new File(file, ".includes");
            if(includes.exists()) {
                final String[] list = Files.readString(includes.toPath()).split("\n");
                final List<File> allSources = new ArrayList<>();

                for(String srcFile : list) {
                    final File file1 = new File(file, srcFile);
                    if(file1.isFile() && file1.exists())
                        allSources.add(file1);
                }

                processSources(output, source.type, source.name, allSources, file, declarations);
            }else
                throw new RuntimeException("Not found includes: " + getName());
        } else if(file.isFile()) {
            processSources(output, source.type, source.name, List.of(file), file.getParentFile(), declarations);
        }

        buildConfigJson.compile.add(new BuildConfigJson.CompileSourceJson(source.name, source.type));
    }

    private boolean clone(File input, File output) throws IOException {
        if(input.exists()) {
            output.getParentFile().mkdirs();

            if(input.isFile())
                Files.write(output.toPath(), Files.readAllBytes(input.toPath()));
            else if(input.isDirectory()) {
                output.mkdirs();
                for(File file : input.listFiles()) {
                    clone(file, new File(output, file.getName()));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void build(File output, File configOutput) {
        try {
            Logs.process("Starting building: " + getName(), "Finished build: " + getName(), () -> {
                try {
                    Files.writeString(new File(output, "mod.info").toPath(), GSON.toJson(make.info));
                    final BuildConfigJson buildConfigJson = new BuildConfigJson();

                    for (SourceDescriptionJson source : make.source) {
                        final List<String> declarations = getDeclarations(source.type);
                        for (String declaration : this.make.declarations) {
                            declarations.add(this.dir.getAbsolutePath() + "/" + declaration);
                        }
                        buildSource(output, source, buildConfigJson, declarations);
                    }

                    for (AssetJson asset : make.assets) {
                        clone(new File(this.dir, asset.path), new File(output, asset.type));
                        buildConfigJson.resources.add(new BuildConfigJson.ResourceJson(asset.type, asset.type));
                    }

                    Files.writeString(new File(output, "build.config").toPath(), GSON.toJson(buildConfigJson));

                    clone(new File(this.dir, make.info.icon), new File(output, "mod_icon.png"));
                    clone(new File(this.dir, make.config), configOutput);

                    for (AdditionallyJson additionally : make.additionally) {
                        final File inputFile = new File(this.dir, additionally.source);
                        clone(inputFile, new File(output, additionally.target + inputFile.getName()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
