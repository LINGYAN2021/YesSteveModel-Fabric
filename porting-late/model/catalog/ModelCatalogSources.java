package com.elfmcys.ysm.model.catalog;

import com.elfmcys.ysm.YesSteveModel;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.List;

public final class ModelCatalogSources {
    private ModelCatalogSources() {
    }

    public static List<ModelCatalogSource> sources() {
        var reloadable = reloadableSources();
        return List.of(builtin(), reloadable.get(0), reloadable.get(1));
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
        return List.of(
                new ModelCatalogSource(CatalogRootKind.CUSTOM, modelRoot.resolve("custom"), true),
                new ModelCatalogSource(CatalogRootKind.AUTH, modelRoot.resolve("auth"), true));
    }

    public static Path customPath() {
        return FabricLoader.getInstance().getGameDir().resolve(YesSteveModel.MOD_ID).resolve("custom");
    }
}
