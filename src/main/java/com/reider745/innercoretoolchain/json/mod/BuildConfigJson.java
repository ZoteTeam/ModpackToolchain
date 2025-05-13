package com.reider745.innercoretoolchain.json.mod;

import java.util.ArrayList;
import java.util.List;

public class BuildConfigJson {
    private static class DefaultConfigJson {
        private final String readme = "this build config is generated automatically by mod development ilya-toolchain";
        private final String api = "CoreEngine";
        private final String buildType = "develop";
    }

    public record CompileSourceJson(String path, String sourceType) {
        @Override
        public String toString() {
            return "CompileSourceJson{" +
                    "path='" + path + '\'' +
                    ", sourceType='" + sourceType + '\'' +
                    '}';
        }
    }

    public record ResourceJson(String path, String resourceType) {
        @Override
        public String toString() {
            return "ResourceJson{" +
                    "path='" + path + '\'' +
                    ", resourceType='" + resourceType + '\'' +
                    '}';
        }
    }

    private final DefaultConfigJson defaultConfig = new DefaultConfigJson();
    public final List<CompileSourceJson> compile = new ArrayList<>();
    public final List<ResourceJson> resources = new ArrayList<>();
    public final List<Object> javaDirs = new ArrayList<>();
    public final List<Object> nativeDirs = new ArrayList<>();
}
