package com.reider745.innercoretoolchain;

import com.google.gson.Gson;
import com.reider745.innercoretoolchain.json.modpack.ModpackMakeJson;
import com.reider745.innercoretoolchain.make.ModpackMake;
import com.reider745.innercoretoolchain.task.TaskRegistry;
import com.reider745.innercoretoolchain.task.impl.BuildModpackTask;
import com.reider745.innercoretoolchain.task.impl.PushTask;
import com.reider745.innercoretoolchain.task.impl.RunServerTask;
import com.reider745.innercoretoolchain.task.impl.UpdateDeclarationsTask;
import com.reider745.innercoretoolchain.type.Side;
import com.reider745.innercoretoolchain.util.DownloadUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Toolchain {
    @Getter private static File declarations;
    @Getter private static File cache;
    @Getter private static File classpath;
    public static final File MAKE = new File("make.json");;
    public static final File WORKING = new File(System.getProperty("user.dir"));
    public static final File TARGET = new File(WORKING, "target");
    public static final File MODPACK = new File(TARGET, "modpacks/dev");
    public static ModpackMakeJson makeJson;

    public static String getDeclarationsPath() {
        return declarations.getAbsolutePath() + "/";
    }

    public static ModpackMakeJson getModpackMakeJson() throws IOException {
        return new Gson().fromJson(Files.readString(MAKE.toPath()), ModpackMakeJson.class);
    }

    public static void main(String[] args) throws Exception {
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

        MODPACK.mkdirs();
        makeJson = getModpackMakeJson();

        TaskRegistry.init();

        declarations = new File(WORKING, "declarations");
        if(declarations.mkdirs()) {
            TaskRegistry.runTask("updateDeclarations");
        }

        for (String task : args) {
            TaskRegistry.runTask(task);
        }
    }
}