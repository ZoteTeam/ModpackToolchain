package com.reider745.innercoretoolchain.make.mod;

import lombok.Getter;

import java.io.File;

@Getter
public abstract class Mod {
    private final String name;

    public Mod(String name) {
        this.name = name;
    }

    public abstract void build(File output, File outputConfig);
}
