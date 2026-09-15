package com.elfmcys.ysm.client.animation.molang.functions;

import com.elfmcys.ysm.geckolib3.core.molang.context.IContext;
import com.elfmcys.ysm.geckolib3.core.molang.function.entity.LivingEntityFunction;
import com.elfmcys.ysm.geckolib3.util.MolangUtils;
import com.elfmcys.ysm.molang.runtime.ExecutionContext;
import com.elfmcys.ysm.util.EquipmentUtil;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;

public class EquippedEnchantmentLevel extends LivingEntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        EquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType == null) {
            return null;
        }
        ItemStack itemStack = EquipmentUtil.getEquippedItem(context.entity().entity(), slotType);
        if (itemStack.isEmpty()) {
            return 0;
        }

        int sum = 0;
        var registry = context.entity().entity().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (var i = 1; i < arguments.size(); ++i) {
            Identifier id = arguments.getAsResourceLocation(context, 1);
            if (id != null) {
                var enchantment = registry.get(ResourceKey.create(Registries.ENCHANTMENT, id));
                if (enchantment.isPresent()) {
                    sum += itemStack.getEnchantments().getLevel(enchantment.get());
                }
            }
        }

        return sum;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 2;
    }
}
