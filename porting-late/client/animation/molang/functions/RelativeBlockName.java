package com.elfmcys.ysm.client.animation.molang.functions;

import com.elfmcys.ysm.geckolib3.core.molang.context.IContext;
import com.elfmcys.ysm.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.ysm.geckolib3.util.MolangUtils;
import com.elfmcys.ysm.molang.runtime.ExecutionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class RelativeBlockName extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> ctx, ArgumentCollection arguments) {
        var block = MolangUtils.getRelativeBlock(ctx, arguments);
        if (block == null) {
            return null;
        }
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block.getBlock());
        if (blockId == null) {
            return null;
        }

        return blockId.toString();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 3;
    }
}
