package com.elfmcys.ysm.client.network;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.capability.AuthModelsCapabilityProvider;
import com.elfmcys.ysm.capability.PlayerAnimatableCapabilityProvider;
import com.elfmcys.ysm.capability.StarModelsCapabilityProvider;
import com.elfmcys.ysm.capability.fabric.EntityCapabilityHolder;
import com.elfmcys.ysm.client.animation.molang.CustomMolangParser;
import com.elfmcys.ysm.client.compat.touhoulittlemaid.TlmCommonCompat;
import com.elfmcys.ysm.geckolib3.core.molang.value.IValue;
import com.elfmcys.ysm.model.domain.Hash256;
import com.elfmcys.ysm.molang.parser.ParseException;
import com.elfmcys.ysm.proto.network.protocol.v0.ControlV0;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * 客户端专用网络处理逻辑。公共处理器（ControlHandler/HandshakeHandler 等）
 * 在服务端也会被类加载与校验，直接引用 Minecraft/LocalPlayer 会在校验期
 * 触发 NoClassDefFoundError，因此客户端逻辑集中到这里，公共侧只做调用。
 */
public final class ClientControlHooks {
    private ClientControlHooks() {
    }

    public static void applyAuthorizedModels(Set<Hash256> hashes) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP)
                    .ifPresent(capability -> capability.setAuthModels(hashes));
        }
    }

    public static void applyStarredModels(Set<Hash256> hashes) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            EntityCapabilityHolder.get(player, StarModelsCapabilityProvider.STAR_MODELS_CAP)
                    .ifPresent(capability -> capability.setStarModels(hashes));
        }
    }

    public static void applyMolangSync(int entityId, us.hebi.quickbuf.RepeatedFloat arguments) {
        var level = Minecraft.getInstance().level;
        var entity = level == null ? null : level.getEntity(entityId);
        if (entity != null) {
            EntityCapabilityHolder.get(entity, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
                var args = new FloatArrayList(arguments.length());
                arguments.forEach(args::add);
                capability.molangSync(args);
            });
        }
    }

    public static void executeMolang(ControlV0.ExecuteMolangEvent message) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        for (var target : message.getTargets()) {
            if (target.hasPlayerId()) {
                continue;
            }
            Entity entity = level.getEntity(target.getEntityId());
            if (entity instanceof Player player) {
                EntityCapabilityHolder.get(player, PlayerAnimatableCapabilityProvider.CAP).ifPresent(capability -> {
                    try {
                        IValue value = CustomMolangParser.parseSingleExpressionUnsafe(message.getExpression());
                        capability.executeMolangExp(value, true, false, null);
                    } catch (ParseException error) {
                        YesSteveModel.LOGGER.error("Failed to execute molang " + message.getExpression(), error);
                    }
                });
            } else if (TlmCommonCompat.isMaid(entity)) {
                TlmCommonCompat.handleExecuteMolang(entity, message.getExpression());
            }
        }
    }

    public static void showVersionMismatch(String local, String peerVersion) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                        "YSM version differs from the server (client " + local
                                + ", server " + peerVersion
                                + "); animation compatibility check passed."));
            }
        });
    }
}
