package com.elfmcys.ysm.mixin;

import com.elfmcys.ysm.network.fabric.YsmConnectionData;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(Connection.class)
public abstract class ConnectionAttributeMixin implements YsmConnectionData {
    @Unique
    private final ConcurrentHashMap<String, Object> ysm$attributes = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<String, Object> ysm$attributes() {
        return ysm$attributes;
    }
}
