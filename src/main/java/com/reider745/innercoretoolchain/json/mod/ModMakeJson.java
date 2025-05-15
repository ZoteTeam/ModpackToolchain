package com.reider745.innercoretoolchain.json.mod;

import java.util.List;

public class ModMakeJson {
    public ModInfoJson info;
    public String config = "config.json";
    public String[] declarations = new String[0];
    public AssetJson[] assets = new AssetJson[0];
    public AdditionallyJson[] additionally = new AdditionallyJson[0];
    public SourceDescriptionJson[] source;

    public ModMakeJson() {}

    public ModMakeJson(String name, String version, String author, String description, List<SourceDescriptionJson> source) {
        this.info = new ModInfoJson(name, version, author, description);
        this.source = source.toArray(new SourceDescriptionJson[0]);
    }
}
