package com.elfmcys.ysm.model.catalog;

import com.elfmcys.ysm.model.source.AccessPolicy;

/** Scanner-local root classification; never appears in source-neutral catalogs or the unstable protocol. */
public enum CatalogRootKind {
    BUILTIN("builtin", AccessPolicy.PUBLIC),
    CUSTOM("custom", AccessPolicy.PUBLIC),
    /**
     * 旧版（2.6.5 及以前）的 config/yes_steve_model/custom 目录。
     * 必须单独一种 kind：扫描与提交校验都假定"一种 kind 对应一个根目录"，
     * 复用 CUSTOM 会把两个根的文件混在一起，从而误判"根目录被修改"并让整个目录同步失败。
     */
    LEGACY_CUSTOM("custom", AccessPolicy.PUBLIC),
    AUTH("auth", AccessPolicy.SESSION_AUTHORIZED);

    private final String namespace;
    private final AccessPolicy accessPolicy;

    CatalogRootKind(String namespace, AccessPolicy accessPolicy) {
        this.namespace = namespace;
        this.accessPolicy = accessPolicy;
    }

    public String namespace() {
        return namespace;
    }

    public AccessPolicy accessPolicy() {
        return accessPolicy;
    }
}
