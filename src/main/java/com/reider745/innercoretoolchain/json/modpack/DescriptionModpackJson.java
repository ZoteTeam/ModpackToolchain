package com.reider745.innercoretoolchain.json.modpack;

import java.util.ArrayList;
import java.util.List;

public class DescriptionModpackJson {
    public String name;
    public String version;
    public String author;
    public String description;
    public final List<DescriptionModJson> mods;
    public ServerDescriptionJson[] servers;

    public DescriptionModpackJson() {
        this.mods = new ArrayList<>();
        this.servers = new ServerDescriptionJson[0];
    }

    public DescriptionModpackJson(String name, String author, String version, String description) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
        this.mods = new ArrayList<>();
        this.servers = new ServerDescriptionJson[0];
    }
}
