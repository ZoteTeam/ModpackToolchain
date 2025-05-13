package com.reider745.innercoretoolchain.json.modpack;

import com.reider745.innercoretoolchain.type.Side;

import java.lang.reflect.Field;

public class ModpackMakeJson extends DescriptionModpackJson {
    public String serverVersion;
    public String proxy;
    public DescriptionModpackJson client;
    public DescriptionModpackJson server;
    public String pushTo;

    public <T>T get(Side side, String name, T def) {
        try {
            final Field field = DescriptionModpackJson.class.getField(name);
            Object value = null;

            if(side == Side.CLIENT) {
                value = field.get(client);
            } else if(side == Side.SERVER) {
                value = field.get(server);
            }

            if(value == null)
                value = field.get(this);

            if(value != null && value.getClass() == def.getClass())
                return (T) value;
            return def;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ManifestModpackJson buildManifest(Side side) {
        final String name = get(side, "name", "");
        final String version = get(side, "version", "");

        return new ManifestModpackJson(
                name, name,
                version,
                get(side, "author", ""),
                get(side, "description", ""),
                version
        );
    }
}
