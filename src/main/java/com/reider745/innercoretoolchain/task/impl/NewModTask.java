package com.reider745.innercoretoolchain.task.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reider745.innercoretoolchain.Toolchain;
import com.reider745.innercoretoolchain.json.mod.ModMakeJson;
import com.reider745.innercoretoolchain.json.mod.SourceDescriptionJson;
import com.reider745.innercoretoolchain.json.modpack.DescriptionModJson;
import com.reider745.innercoretoolchain.task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NewModTask extends Task {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void run() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Name:");
        final String modName = scanner.nextLine();

        System.out.println("Version:");
        final String modVersion = scanner.nextLine();

        System.out.println("Author:");
        final String modAuthor = scanner.nextLine();

        System.out.println("Description:");
        final String modDescription = scanner.nextLine();

        System.out.println("Client only(Y/n):");
        final boolean isClient = scanner.nextLine().equalsIgnoreCase("y");

        System.out.println("Use javascript\\typescript(Y/n):");
        final boolean isJavascript = scanner.nextLine().equalsIgnoreCase("y");

        System.out.println("Use java(Y/n):");
        final boolean isJava = scanner.nextLine().equalsIgnoreCase("y");



        final File mods = new File(Toolchain.getWORKING(), "mods");
        final File mod = new File(mods, modName);

        final List<SourceDescriptionJson> sources = new ArrayList<>();

        sources.add(new SourceDescriptionJson("launcher", null, "launcher.js", null, null));

        if(isJavascript)
            sources.add(new SourceDescriptionJson("mod", "main.js", "dev", null, null));

        String bootClass = null;

        if(isJava) {
            System.out.println("Boot-Class:");
            bootClass = scanner.nextLine();
            sources.add(new SourceDescriptionJson("java", "java", "java", null, new String[] {bootClass}));
        }

        mod.mkdirs();

        try {
            Files.writeString(new File(mod, "launcher.js").toPath(), "ConfigureMultiplayer({\n" +
                    "    isClientOnly:  " + isClient + "\n" +
                    "});\n" +
                    "Launch();");

            if(isJavascript) {
                final File dev = new File(mod, "dev");
                dev.mkdirs();

                Files.writeString(new File(dev,".includes").toPath(), "header.ts");
                Files.writeString(new File(dev, "header.ts").toPath(), "alert(\"Hello world!\")");
            }

            if(isJava && bootClass != null && !bootClass.isEmpty()) {
                final File bootClassFile = new File(mod, "java/" + bootClass.replace('.', '/') + ".java");
                bootClassFile.getParentFile().mkdirs();

                final StringBuilder javaBoot = new StringBuilder();


                javaBoot.append("package " + bootClass.replace("."  + bootClassFile.getName().replace(".java", ""), "") + ";\n");
                javaBoot.append("\n");
                javaBoot.append("import com.zhekasmirnov.horizon.runtime.logger.Logger;\n");
                javaBoot.append("import java.util.HashMap;\n");
                javaBoot.append("\n");
                javaBoot.append("public class " + bootClassFile.getName().replace(".java", "") + " {\n");
                javaBoot.append("\tpublic static void boot(HashMap<?, ?> args) {\n");
                javaBoot.append("\t\tLogger.debug(\"Hello world!\");\n");
                javaBoot.append("\t}\n");
                javaBoot.append("}");

                Files.writeString(bootClassFile.toPath(), javaBoot.toString());
            }

            if(isClient) {
                Toolchain.getMakeJson().client.mods.add(new DescriptionModJson("mods/" + modName));
            } else {
                Toolchain.getMakeJson().mods.add(new DescriptionModJson("mods/" + modName));
            }

            Toolchain.saveMake();

            Files.writeString(new File(mod,"config.json").toPath(), "{\"enabled\": true}");
            Files.writeString(new File(mod, "make.json").toPath(), GSON.toJson(
                    new ModMakeJson(modName, modVersion, modAuthor, modDescription, sources)
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
