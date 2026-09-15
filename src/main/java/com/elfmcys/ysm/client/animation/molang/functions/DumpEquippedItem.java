package com.elfmcys.ysm.client.animation.molang.functions;

import com.elfmcys.ysm.geckolib3.core.molang.context.IContext;
import com.elfmcys.ysm.geckolib3.core.molang.function.entity.LivingEntityFunction;
import com.elfmcys.ysm.geckolib3.util.MolangUtils;
import com.elfmcys.ysm.molang.runtime.ExecutionContext;
import com.elfmcys.ysm.util.EquipmentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;


public class DumpEquippedItem extends LivingEntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        if (!context.entity().isDebugEnabled()) {
            return null;
        }

        EquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType == null) {
            return null;
        }

        ItemStack itemStack = EquipmentUtil.getEquippedItem(context.entity().entity(), slotType);
        if (itemStack.isEmpty()) {
            return null;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (id == null) {
            return null;
        }
        context.entity().debugPrint(Component.literal("Display ").append(ComponentUtils.copyOnClickText(itemStack.getItem().getName(itemStack).getString())));
        context.entity().debugPrint(Component.literal("Name ").append(ComponentUtils.copyOnClickText(id.toString())));

        itemStack.typeHolder().tags().forEach(key -> {
            context.entity().debugPrint(Component.literal("Tag ").append(ComponentUtils.copyOnClickText(key.location().toString())));
        });

        // 26.1.2：附魔存储在 ItemEnchantments 数据组件中
        for (var entry : itemStack.getEnchantments().entrySet()) {
            var holder = entry.getKey();
            int level = entry.getIntValue();
            holder.unwrapKey().ifPresent(key -> {
                context.entity().debugPrint(Component.literal("Enchantment: display ").append(ComponentUtils.copyOnClickText(Enchantment.getFullname(holder, level).getString()))
                        .append(Component.literal("  name ").append(ComponentUtils.copyOnClickText(key.identifier().toString()))));
            });
        }

        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
