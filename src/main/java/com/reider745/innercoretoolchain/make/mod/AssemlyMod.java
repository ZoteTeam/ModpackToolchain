package com.reider745.innercoretoolchain.make.mod;

import com.android.tools.r8.SwissArmyKnife;
import com.google.gson.Gson;
import com.reider745.innercoretoolchain.Toolchain;
import com.reider745.innercoretoolchain.json.mod.*;
import com.reider745.innercoretoolchain.util.Logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            declarations.add(Toolchain.getDeclarationsPath() + "declarations/launcher.d.ts");

        declarations.add(Toolchain.getDeclarationsPath() + "core-engine.d.ts");
        declarations.add(Toolchain.getDeclarationsPath() + "android.d.ts");

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

    public File getGradleBuild() {
        return new File(Toolchain.getCache(), "build-" + getName());
    }

    private void installGradle(SourceDescriptionJson source) throws IOException {
        final ClassLoader classLoader = AssemlyMod.class.getClassLoader();
        final File gradlew = new File(dir, "gradle/gradlew");
        if(!gradlew.exists())
            Files.write(gradlew.toPath(), classLoader.getResourceAsStream(gradlew.getName()).readAllBytes());
        final File gradlewBat = new File(dir, "gradle/gradlew.bat");
        if(!gradlewBat.exists())
            Files.write(gradlewBat.toPath(), classLoader.getResourceAsStream(gradlewBat.getName()).readAllBytes());

        String gradle = new String(classLoader.getResourceAsStream("gradle/build.gradle").readAllBytes());

        gradle = gradle.replace("{src}", source.source);
        gradle = gradle.replace("{buildDir}", getGradleBuild().getAbsolutePath());
        gradle = gradle.replace("{classpath}", Toolchain.getClasspath().getAbsolutePath());

        Files.writeString(new File(this.dir, "gradle/build.gradle").toPath(), gradle);

        final File settings = new File(this.dir, "settings.gradle");
        if(!settings.exists()) {
            Files.writeString(settings.toPath(), "rootProject.name = '" + getName() + "'");
        }

        final File wrapper = new File(this.dir, "gradle/wrapper");
        wrapper.mkdirs();

        final File gradleWrapperJar = new File(wrapper, "gradle/gradle-wrapper.jar");
        if(!gradleWrapperJar.exists()) {
            Files.write(gradleWrapperJar.toPath(), classLoader.getResourceAsStream(gradleWrapperJar.getName()).readAllBytes());
        }

        final File gradleWrapperProperties = new File(wrapper, "gradle/gradle-wrapper.properties");
        if(!gradleWrapperProperties.exists()) {
            Files.write(gradleWrapperProperties.toPath(), classLoader.getResourceAsStream(gradleWrapperProperties.getName()).readAllBytes());
        }
    }

    private void buildJava(File output, SourceDescriptionJson source, BuildConfigJson config) throws IOException {
        installGradle(source);

        final ProcessBuilder pb = new ProcessBuilder();
        pb.directory(this.dir);
        pb.inheritIO();

        String mainFile = this.dir.getAbsolutePath() + "/gradle/gradlew";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            mainFile = mainFile.replace("sh ", "") + ".bat";
            pb.command(mainFile, "buildAndDowngrade");
        } else
            pb.command("sh", mainFile, "buildAndDowngrade");

        try {
            pb.start().waitFor();
            final File outputJava = new File(output, "java");
            outputJava.mkdirs();
            SwissArmyKnife.main(new String[] {
                "d8", "--min-api", "19", "--release", "--output", outputJava.getAbsolutePath(),
                    new File(getGradleBuild(), "libs/" + getName() +"-downgraded-8.jar").getAbsolutePath()
            });

            final Map<String, Object> manifest = new HashMap<>();

            manifest.put("source-dirs", new String[0]);
            manifest.put("library-dirs", new String[0]);
            manifest.put("verbose", true);
            manifest.put("options", new String[0]);
            manifest.put("boot-classes", source.boot);

            Files.writeString(new File(outputJava, "manifest").toPath(), GSON.toJson(manifest));

            config.javaDirs.add(new BuildConfigJson.PathJson("java"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildSource(File output, SourceDescriptionJson source, BuildConfigJson buildConfigJson, List<String> declarations) throws IOException {
        if(source.type.equals("java")) {
            buildJava(output, source, buildConfigJson);
            return;
        }

        final File file = new File(dir, source.source);
        if(!file.exists())
            return;

        if(source.type.equals("library")) {
            source.fast = true;
            final File libs = new File(output, "libs");
            libs.mkdirs();
            source.name = "libs/" + new File(libs, source.source).getName();
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
