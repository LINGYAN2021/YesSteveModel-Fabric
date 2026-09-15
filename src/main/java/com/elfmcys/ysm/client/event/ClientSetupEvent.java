package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.animation.AnimationRegister;
import com.elfmcys.ysm.client.compat.ARCompat;
import com.elfmcys.ysm.client.compat.CosmeticArmorCompat;
import com.elfmcys.ysm.client.compat.ElytraSlotCompat;
import com.elfmcys.ysm.client.compat.FirstPersonCompat;
import com.elfmcys.ysm.client.compat.ImmersiveAircraftCompat;
import com.elfmcys.ysm.client.compat.IrisCompat;
import com.elfmcys.ysm.client.compat.OptifineCompat;
import com.elfmcys.ysm.client.compat.PlayerAnimatorCompat;
import com.elfmcys.ysm.client.compat.SimplePlaneCompat;
import com.elfmcys.ysm.client.compat.backpack.sophisticated.SophisticatedCompat;
import com.elfmcys.ysm.client.compat.bettercombat.BetterCombatCompat;
import com.elfmcys.ysm.client.compat.carryon.CarryOnCompat;
import com.elfmcys.ysm.client.compat.create.CreateCompat;
import com.elfmcys.ysm.client.compat.curios.CuriosCompat;
import com.elfmcys.ysm.client.compat.immersivemelodies.ImmersiveMelodiesCompat;
import com.elfmcys.ysm.client.compat.ironsspellbooks.IronsSpellBooksCompat;
import com.elfmcys.ysm.client.compat.parcool.ParCoolCompat;
import com.elfmcys.ysm.client.compat.realcamera.RealCameraCompat;
import com.elfmcys.ysm.client.compat.simplehat.SimpleHatsCompat;
import com.elfmcys.ysm.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.ysm.client.compat.swarfare.SWarfareCompat;
import com.elfmcys.ysm.client.compat.swem.SwemCompat;
import com.elfmcys.ysm.client.compat.tacz.TACZCompat;
import com.elfmcys.ysm.client.compat.touhoulittlemaid.client.TlmClientCompat;
import com.elfmcys.ysm.client.gui.overlay.DebugAnimationScreen;
import com.elfmcys.ysm.client.gui.overlay.ExtraPlayerScreen;
import com.elfmcys.ysm.client.gui.overlay.LoadingStateScreen;
import com.elfmcys.ysm.client.input.AnimationRouletteKey;
import com.elfmcys.ysm.client.input.DebugAnimationKey;
import com.elfmcys.ysm.client.input.ExtraAnimationKey;
import com.elfmcys.ysm.client.input.ExtraPlayerConfigKey;
import com.elfmcys.ysm.client.input.PlayerModelScreenKey;
import com.elfmcys.ysm.client.model.PlayerLocator;
import com.elfmcys.ysm.client.model.ClientModelService;
import com.elfmcys.ysm.config.ClientConfig;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class ClientSetupEvent {
    public static void onClientSetup() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }

        AnimationRegister.registerAnimationState();

        CuriosCompat.init();
        FirstPersonCompat.init();
        RealCameraCompat.init();
        PlayerAnimatorCompat.init();
        BetterCombatCompat.init();
        IrisCompat.init();
        ARCompat.init();
        OptifineCompat.init();
        CosmeticArmorCompat.init();
        ElytraSlotCompat.init();
        TACZCompat.init();
        SWarfareCompat.init();
        TlmClientCompat.init();
        CarryOnCompat.init();
        ParCoolCompat.init();
        SlashBladeCompat.init();
        SwemCompat.init();
        CreateCompat.init();
        SophisticatedCompat.init();
        SimpleHatsCompat.init();
        ImmersiveMelodiesCompat.init();
        IronsSpellBooksCompat.init();
        SimplePlaneCompat.init();
        ImmersiveAircraftCompat.init();

        checkCompatibility(ParCoolCompat.getCompatibilityWarning());
        checkCompatibility(SophisticatedCompat.getCompatibilityWarning());
        if (ClientConfig.DISABLE_SELF_MODEL.get() &&
                ClientConfig.DISABLE_OTHER_MODEL.get() &&
                ClientConfig.DISABLE_SELF_HANDS.get()) {
            informIncompatible("epicfight", "Epic Fight");
        }

        // Model render target data is now owned by the Java model service.
        PlayerLocator.init();
        ClientModelService.start();
    }

    private static void checkCompatibility(Optional<Pair<String, String>> infoHolder) {
        infoHolder.ifPresent(info -> YesSteveModel.LOGGER.warn(
                "Incompatible mod version detected: {} {}", info.getKey(), info.getValue()));
    }

    private static void informIncompatible(String modId, String modName) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            YesSteveModel.LOGGER.warn("Incompatible mod detected: {}", modName);
        }
    }

    public static void registerKeyMappings() {
        KeyMappingHelper.registerKeyMapping(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        PlayerModelScreenKey.register();

        if (!YesSteveModel.isAvailable()) {
            return;
        }

        KeyMappingHelper.registerKeyMapping(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        KeyMappingHelper.registerKeyMapping(AnimationRouletteKey.LOCK_ROULETTE_KEY);
        KeyMappingHelper.registerKeyMapping(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        KeyMappingHelper.registerKeyMapping(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        AnimationRouletteKey.register();
        DebugAnimationKey.register();
        ExtraPlayerConfigKey.register();
        ExtraAnimationKey.register();
    }

    public static void registerGuiOverlays() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "ysm_debug_info"),
                DebugAnimationScreen.getHudElement());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "ysm_extra_player"),
                new ExtraPlayerScreen());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "ysm_loading_state"),
                new LoadingStateScreen());
    }
}
