package com.elfmcys.ysm.model.catalog;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModelCatalogSources {
    private ModelCatalogSources() {
    }

    public static List<ModelCatalogSource> sources() {
        var result = new ArrayList<ModelCatalogSource>();
        result.add(builtin());
        result.addAll(reloadableSources());
        return List.copyOf(result);
    }

    public static ModelCatalogSource builtin() {
        var builtin = FabricLoader.getInstance().getModContainer(YesSteveModel.MOD_ID)
                .orElseThrow().findPath("assets/" + YesSteveModel.MOD_ID + "/builtin")
                .orElseThrow();
        return new ModelCatalogSource(CatalogRootKind.BUILTIN, builtin, false);
    }

    public static Path builtinIndex() {
        return FabricLoader.getInstance().getModContainer(YesSteveModel.MOD_ID)
                .orElseThrow().findPath("assets/" + YesSteveModel.MOD_ID + "/builtin-index.json")
                .orElseThrow();
    }

    public static List<ModelCatalogSource> reloadableSources() {
        var modelRoot = FabricLoader.getInstance().getGameDir().resolve(YesSteveModel.MOD_ID);
        var result = new ArrayList<ModelCatalogSource>();
        result.add(new ModelCatalogSource(CatalogRootKind.CUSTOM, modelRoot.resolve("custom"), true));
        result.add(new ModelCatalogSource(CatalogRootKind.AUTH, modelRoot.resolve("auth"), true));
        // 旧版（2.6.5 及以前）自定义模型放在 config/yes_steve_model/custom，
        // 升级后这里的老模型包 (.ysm) 仍要能扫到，否则玩家会以为模型全丢了
        var legacy = legacyCustomPath();
        if (Files.isDirectory(legacy)) {
            result.add(new ModelCatalogSource(CatalogRootKind.LEGACY_CUSTOM, legacy, false));
        }
        return List.copyOf(result);
    }

    public static Path customPath() {
        return FabricLoader.getInstance().getGameDir().resolve(YesSteveModel.MOD_ID).resolve("custom");
    }

    /**
     * 旧版模型目录（只读取沿用，不主动创建）
     */
    public static Path legacyCustomPath() {
        return FabricLoader.getInstance().getGameDir()
                .resolve("config").resolve("yes_steve_model").resolve("custom");
    }
}
