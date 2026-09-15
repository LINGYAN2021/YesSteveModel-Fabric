package com.elfmcys.ysm.client.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.gui.DownloadScreen;
import com.elfmcys.ysm.client.gui.PlayerModelScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class DownloadScreenInterModEvent {
    /**
     * Fabric 没有 IMC，改为入口点：其他模组可以注册
     * "ysm:download_screen" 入口点（Supplier&lt;Screen&gt;）来自定义下载界面。
     */
    private static final String DOWNLOAD_SCREEN_ENTRYPOINT = "ysm:download_screen";
    private static @Nullable Screen DOWNLOAD_SCREEN;

    @SuppressWarnings("unchecked")
    public static void onInitializeClient() {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        FabricLoader.getInstance().getEntrypointContainers(DOWNLOAD_SCREEN_ENTRYPOINT, Object.class).stream()
                .findFirst()
                .ifPresent(container -> {
                    Object entrypoint = container.getEntrypoint();
                    if (entrypoint instanceof Supplier<?> supplier && supplier.get() instanceof Screen screen) {
                        DOWNLOAD_SCREEN = screen;
                    } else if (entrypoint instanceof Screen screen) {
                        DOWNLOAD_SCREEN = screen;
                    }
                });
    }

    public static void openDownloadScreen(PlayerModelScreen modelScreen) {
        Minecraft.getInstance().setScreen(Objects.requireNonNullElseGet(DOWNLOAD_SCREEN, () -> new DownloadScreen(modelScreen)));
    }
}
