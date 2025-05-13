package com.reider745.innercoretoolchain.util.icmods;

import com.google.gson.Gson;
import com.reider745.innercoretoolchain.util.icmods.json.ModDescriptionJson;
import com.reider745.innercoretoolchain.util.DownloadUtil;

import java.io.IOException;

public class IcmodMod {
    private static final Gson GSON = new Gson();

    private final int id;
    private final String proxy;
    private final ModDescriptionJson description;

    public IcmodMod(String proxy, int id) throws IOException {
        this.proxy = proxy;
        this.id = id;
        this.description = GSON.fromJson(DownloadUtil.readStringHttp("https://" + proxy + "/api/description?id=" + id), ModDescriptionJson.class);
    }

    public int getVersion() {
        return description.version;
    }

    public String getName() {
        return description.title;
    }

    public byte[] download() throws IOException {
        return DownloadUtil.readHttp("https://" + proxy + "/api/download?id=" + id);
    }
}
