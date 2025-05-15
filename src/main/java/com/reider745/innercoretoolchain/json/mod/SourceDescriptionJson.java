package com.reider745.innercoretoolchain.json.mod;

public class SourceDescriptionJson {
    public String type;
    public String name;
    public String source;

    public Boolean fast = false;
    public String[] boot = new String[0];

    public SourceDescriptionJson() {}

    public SourceDescriptionJson(String type, String name, String source, Boolean fast, String[] boot) {
        this.type = type;
        this.name = name;
        this.source = source;
        this.fast = fast;
        this.boot = boot;
    }
}
