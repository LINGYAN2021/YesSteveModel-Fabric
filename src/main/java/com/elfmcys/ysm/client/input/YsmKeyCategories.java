package com.elfmcys.ysm.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * 26.1.2 中 KeyMapping 的分类改为 {@link KeyMapping.Category} 记录。
 * 使用默认命名空间 + 路径 yes_steve_model，使语言键保持 key.category.yes_steve_model 不变。
 */
public final class YsmKeyCategories {
    public static final KeyMapping.Category YSM =
            KeyMapping.Category.register(Identifier.withDefaultNamespace("yes_steve_model"));

    private YsmKeyCategories() {
    }
}
