package com.reider745.innercoretoolchain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reider745.innercoretoolchain.json.modpack.ModpackMakeJson;
import com.reider745.innercoretoolchain.task.TaskRegistry;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

public class Toolchain {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter public static final File CACHE = new File("cache");
    @Getter public static final File CLASSPATH = new File("classpath");
    @Getter public static final File MAKE = new File("make.json");;
    @Getter public static final File WORKING = new File(System.getProperty("user.dir"));
    @Getter public static final File TARGET = new File(WORKING, "target");
    @Getter public static final File MODPACK = new File(TARGET, "modpacks/dev");
    @Getter public static final File DECLARATIONS = new File(WORKING, "declarations");;

    @Getter public static ModpackMakeJson makeJson;

    public static String getDeclarationsPath() {
        return DECLARATIONS.getAbsolutePath() + "/";
    }

    public static ModpackMakeJson getModpackMakeJson() throws IOException {
        return GSON.fromJson(Files.readString(MAKE.toPath()), ModpackMakeJson.class);
    }

    public static void saveMake() throws IOException {
        Files.writeString(MAKE.toPath(), GSON.toJson(makeJson));
    }

    private static void installComponents() {
        if(DECLARATIONS.mkdirs()) TaskRegistry.runTask("updateDeclarations");
        TaskRegistry.runTask("updateClasspath");
    }

    public static void main(String[] args) throws Exception {
        if(!MAKE.exists()) {
            final Scanner scanner = new Scanner(System.in);

            System.out.print("Push to android folder: ");
            String pushTo = scanner.nextLine();
            if(pushTo.isEmpty()) pushTo = "/storage/emulated/0/Android/media/com.zheka.horizon64/packs/Inner_Core__ARM64_/modpacks";

            System.out.print("Name: ");
            final String modpackName = scanner.nextLine();

            System.out.print("Author: ");
            final String author = scanner.nextLine();

            System.out.print("Version: ");
            final String version = scanner.nextLine();

            System.out.print("Description: ");
            final String description = scanner.nextLine();

            Files.writeString(MAKE.toPath(), GSON.toJson(new ModpackMakeJson(pushTo, modpackName, author, version, description)));

            final File vscode = new File(WORKING, ".vscode/tasks.json");
            vscode.getParentFile().mkdirs();
            if(!vscode.exists())
                Files.write(vscode.toPath(), Toolchain.class.getClassLoader().getResourceAsStream("tasks.json").readAllBytes());
            return;
        }

        CACHE.mkdir();
        CLASSPATH.mkdir();
        MODPACK.mkdirs();

        makeJson = getModpackMakeJson();

        TaskRegistry.init();
        installComponents();

        Arrays.stream(args).forEach(TaskRegistry::runTask);
    }
}