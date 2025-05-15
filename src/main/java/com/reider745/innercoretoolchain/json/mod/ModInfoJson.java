package com.reider745.innercoretoolchain.json.mod;

public class ModInfoJson {
    public String name;
    public String version;
    public String author;
    public String description;
    public String icon = "mod_icon.png";

    public ModInfoJson() {}

    public ModInfoJson(String name, String version, String author, String description) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
    }
}
