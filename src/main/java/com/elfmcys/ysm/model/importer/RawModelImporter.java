package com.elfmcys.ysm.model.importer;

import com.elfmcys.ysm.format.parser.ModelParser;
import com.elfmcys.ysm.format.parser.DefaultAnimationFilter;
import com.elfmcys.ysm.format.parser.RawCompileResult;
import com.elfmcys.ysm.format.vfs.Directory;
import com.elfmcys.ysm.model.catalog.CatalogModelLocation;
import com.elfmcys.ysm.model.catalog.CatalogRootKind;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.model.domain.ModelPath;
import com.elfmcys.ysm.model.storage.ModelFileHandle;
import com.elfmcys.ysm.natives.NativeArchive;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class RawModelImporter {
    private final LegacyImporter v3Importer;
    private final DefaultAnimationFilter defaultAnimations;

    public RawModelImporter(LegacyImporter v3Importer,
                            DefaultAnimationFilter defaultAnimations) {
        this.v3Importer = Objects.requireNonNull(v3Importer, "v3Importer");
        this.defaultAnimations = Objects.requireNonNull(defaultAnimations, "defaultAnimations");
    }

    public RawCompileResult convert(Path source, Path outputDirectory) {
        // 旧版加密 .ysm（YSGP v3）由 native 的 LegacyBundle 就在 NativeArchive 里解密，
        // 不需要走单独的导入器 —— 之前在这里提前抛"unavailable"把 55 个旧模型全挡在门外
        if (Files.isDirectory(source)) {
            try (var vfs = new Directory(source)) {
                return ModelParser.compile(vfs, outputDirectory, defaultAnimations);
            }
        }
        try (var vfs = new NativeArchive(source.toString())) {
            return ModelParser.compile(vfs, outputDirectory, defaultAnimations);
        }
    }

    public boolean supportsHashProbe(Path source) {
        // 加密 .ysm 同样能通过 NativeArchive 解密后做 dry-run 探测
        return true;
    }

    public Hash256 scanModelHash(Path source) {
        if (Files.isDirectory(source)) {
            try (var vfs = new Directory(source)) {
                return ModelParser.scanModelHash(vfs);
            }
        }
        try (var vfs = new NativeArchive(source.toString())) {
            return ModelParser.scanModelHash(vfs);
        }
    }
}
