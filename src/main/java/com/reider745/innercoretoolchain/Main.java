package com.reider745.innercoretoolchain;

import com.google.gson.Gson;
import com.reider745.innercoretoolchain.json.modpack.ModpackMakeJson;
import com.reider745.innercoretoolchain.make.ModpackMake;
import com.reider745.innercoretoolchain.task.BuildModpackTask;
import com.reider745.innercoretoolchain.task.PushTask;
import com.reider745.innercoretoolchain.task.RunServerTask;
import com.reider745.innercoretoolchain.task.UpdateDeclarationsTask;
import com.reider745.innercoretoolchain.type.Side;
import com.reider745.innercoretoolchain.util.DownloadUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    @Getter
    private static File declarations;
    @Getter
    private static File cache;
    @Getter
    private static File classpath;

    private static File make;

    public static String getDeclarationsPath() {
        return declarations.getAbsolutePath() + "/";
    }

    public static ModpackMakeJson getModpackMakeJson() throws IOException {
        return new Gson().fromJson(Files.readString(make.toPath()), ModpackMakeJson.class);
    }

    public static void main(String[] args) throws Exception {
        final File WORKING = new File(System.getProperty("user.dir"));
        final File TARGET = new File(WORKING, "target");
        make = new File("make.json");
        final File MODPACK = new File(TARGET, "modpacks/dev");

        cache = new File("cache");
        cache.mkdir();

        classpath = new File("classpath");
        classpath.mkdir();

        {
            final String[] files = new String[] {
                    "android.jar",
                    "horizon-1.2.jar",
                    "innercore-test.jar"
            };

            for(String file : files) {
                final File outputJava = new File(classpath, file);
                if (!outputJava.exists()) {
                    Files.write(outputJava.toPath(), DownloadUtil.readHttp("https://raw.githubusercontent.com/zheka2304/innercore-mod-toolchain/refs/heads/develop/toolchain/toolchain/classpath/" + file));
                }
            }
        }

        declarations = new File(WORKING, "declarations");
        if(declarations.mkdirs()) {
            new UpdateDeclarationsTask().execute();
        }


        MODPACK.mkdirs();

        final ModpackMakeJson makeJson = getModpackMakeJson();

        for (String task : args) {
            switch (task) {
                case "buildClient" -> new BuildModpackTask(Side.CLIENT, WORKING, null, make)
                        .execute();

                case "buildServer" -> new BuildModpackTask(Side.SERVER, WORKING, MODPACK, make)
                        .execute();

                case "runServer" -> new RunServerTask(TARGET, "https://github.com/Reider745/ZoteCoreLoader/releases/download/" + makeJson.serverVersion + "/ZoteCoreLoader.jar")
                        .execute();

                case "push" -> new PushTask(new ModpackMake(WORKING, make).getOutput(Side.CLIENT).getAbsolutePath(), makeJson.pushTo)
                        .execute();

                case "stop" -> new ProcessBuilder("adb", "shell", "am", "force-stop", "com.zheka.horizon64").start().waitFor();

                case "launch" -> {
                    new ProcessBuilder("adb", "shell", "touch", "/storage/emulated/0/games/horizon/.flag_auto_launch").start().waitFor();
                    new ProcessBuilder("adb", "shell", "monkey", "-p", "com.zheka.horizon64", "-c", "android.intent.category.LAUNCHER", "1").start().waitFor();
                }
            }
        }
    }
}