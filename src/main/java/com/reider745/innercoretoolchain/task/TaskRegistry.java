package com.reider745.innercoretoolchain.task;

import com.reider745.innercoretoolchain.make.ModpackMake;
import com.reider745.innercoretoolchain.task.impl.*;
import com.reider745.innercoretoolchain.type.Side;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.reider745.innercoretoolchain.Toolchain.*;

public class TaskRegistry {
    private static final Map<String, Task> taskMap = new HashMap<>();

    public static void init() throws IOException {
        register("buildClient", new BuildModpackTask(Side.CLIENT, WORKING, null, MAKE));
        register("buildServer", new BuildModpackTask(Side.SERVER, WORKING, MODPACK, MAKE));
        register("runServer", new RunServerTask(TARGET, "https://github.com/Reider745/ZoteCoreLoader/releases/download/" + makeJson.serverVersion + "/ZoteCoreLoader.jar"));
        register("push", new PushTask(new ModpackMake(WORKING, MAKE).getOutput(Side.CLIENT).getAbsolutePath(), makeJson.pushTo));
        register("stop", new ProcessBuildersTask(new ProcessBuilder("adb", "shell", "am", "force-stop", "com.zheka.horizon64")));
        register("launch", new ProcessBuildersTask(new ProcessBuilder("adb", "shell", "touch", "/storage/emulated/0/games/horizon/.flag_auto_launch"),
                new ProcessBuilder("adb", "shell", "monkey", "-p", "com.zheka.horizon64", "-c", "android.intent.category.LAUNCHER", "1")));
        register("updateDeclarations", new UpdateDeclarationsTask());
    }

    public static void runTask(String name) {
        if(taskMap.containsKey(name)) {
            taskMap.get(name).execute();
            return;
        }

        throw new RuntimeException("Task " + name + " not found");
    }

    private static void register(String name, Task task) {
        taskMap.put(name, task);
    }
}
