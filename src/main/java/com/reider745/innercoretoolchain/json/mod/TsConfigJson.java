package com.reider745.innercoretoolchain.json.mod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TsConfigJson {
    private static class CompilerOptions {
        private final String outFile;
        private final String target = "es5";
        private final String[] lib = new String[] {
                "es5",
                "es2015.core",
                "es2015.generator"
        };
        private final String module = "none";
        private final boolean skipDefaultLibCheck = true;
        private final boolean composite = false;
        private final boolean incremental = false;
        private final boolean declaration = false;
        private final boolean downlevelIteration = true;
        private final boolean experimentalDecorators = true;
        private final boolean noEmitOnError = true;
        private final boolean stripInternal = true;
        private final boolean allowJs = true;
        private final String[] types = new String[0];

        public CompilerOptions(String outFile) {
            this.outFile = outFile;
            //this.rootDir = rootDir;
        }
    }

    private final CompilerOptions compilerOptions;
    private final boolean compileOnSave = false;
    private final String[] exclude = new String[] {
            "dom",
            "dom.iterable",
            "es2015.iterable",
            "scripthost",
            "webworker",
            "webworker.importscripts",
            "webworker.iterable"
    };
    private final String[] include;
    private final String[] files;

    public TsConfigJson(String outDir, List<File> includes, List<String> files) {
        this.compilerOptions = new CompilerOptions(outDir);
        this.include = new String[includes.size()];

        for(int i = 0;i < includes.size();i++)
            this.include[i] = includes.get(i).getAbsolutePath();

        this.files = files.toArray(new String[0]);
    }
}
