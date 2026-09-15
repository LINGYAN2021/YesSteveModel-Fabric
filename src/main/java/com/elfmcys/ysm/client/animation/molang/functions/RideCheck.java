package com.elfmcys.ysm.client.animation.molang.functions;

import com.elfmcys.ysm.geckolib3.core.molang.context.IContext;
import com.elfmcys.ysm.geckolib3.core.molang.function.entity.LivingEntityFunction;
import com.elfmcys.ysm.molang.runtime.ExecutionContext;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.core.registries.Registries;

public class RideCheck extends LivingEntityFunction {
    private static final String ID_PREFIX = "$";
    private static final String TAG_PREFIX = "#";
    private static final String VEHICLE = "vehicle";
    private static final String PASSENGER = "passenger";
    private static final int FALSE = 0;
    private static final int TRUE = 1;

    public static RideCheck rideCheck() {
        return new RideCheck();
    }

    @Override
    protected Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        String type = arguments.getAsString(context, 0);
        String input = arguments.getAsString(context, 1);
        LivingEntity entity = context.entity().entity();

        if (StringUtils.isBlank(input)) {
            return FALSE;
        }

        Entity checkEntity;
        if (VEHICLE.equals(type)) {
            checkEntity = entity.getVehicle();
        } else if (PASSENGER.equals(type)) {
            checkEntity = entity.getFirstPassenger();
        } else {
            return FALSE;
        }

        if (checkEntity == null || !checkEntity.isAlive()) {
            return FALSE;
        }

        String subInput = input.substring(1);
        EntityType<?> entityType = checkEntity.getType();
        if (input.startsWith(ID_PREFIX)) {
            Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (registryName == null) {
                return FALSE;
            }
            boolean equals = subInput.equals(registryName.toString());
            return equals ? TRUE : FALSE;
        }

        if (input.startsWith(TAG_PREFIX)) {
            Identifier tag = Identifier.parse(subInput);
            TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tag);
            return entityType.builtInRegistryHolder().is(tagKey) ? TRUE : FALSE;
        }

        return FALSE;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2 || size == 3;
    }
}
