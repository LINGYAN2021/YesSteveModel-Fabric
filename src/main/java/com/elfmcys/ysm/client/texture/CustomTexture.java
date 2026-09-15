package com.elfmcys.ysm.client.texture;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.client.model.ModelResourceFailureGate;
import com.elfmcys.ysm.natives.image.ImageSource;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class CustomTexture extends AbstractTexture {
    private final ImageSource source;
    private final Executor workers;
    private final ModelResourceFailureGate failureGate;
    private final TextureLoadState loadState = new TextureLoadState();

    public CustomTexture(ImageSource source, Executor workers) {
        this(source, workers, ModelResourceFailureGate.none());
    }

    public CustomTexture(ImageSource source, Executor workers,
                         ModelResourceFailureGate failureGate) {
        this.source = Objects.requireNonNull(source, "source");
        this.workers = Objects.requireNonNull(workers, "workers");
        this.failureGate = Objects.requireNonNull(failureGate, "failureGate");
    }

    // 26.1.2 的 AbstractTexture 不再有 load 回调；由 YSM 自身逻辑调用
    public void load(ResourceManager resourceManager) {
        if (failureGate.failure().isPresent()) {
            return;
        }
        var start = loadState.begin();
        if (start == null) {
            return;
        }
        if (start.previous() != null) {
            start.previous().cancel(true);
        }

        final CompletableFuture<Void> task;
        try {
            task = CompletableFuture.runAsync(() -> readAndDispatch(start.generation()), workers);
        } catch (RuntimeException error) {
            if (fail(start.generation(), error)) {
                YesSteveModel.LOGGER.debug("Failed to schedule model texture load from {}", source, error);
            }
            return;
        }
        if (!loadState.attach(start.generation(), task)) {
            task.cancel(true);
        }
        task.whenComplete((ignored, error) -> {
            if (error != null && !(unwrap(error) instanceof CancellationException)) {
                var cause = unwrap(error);
                if (fail(start.generation(), cause)) {
                    YesSteveModel.LOGGER.debug("Failed to load model texture from {}", source, cause);
                }
            } else {
                loadState.complete(start.generation(), task);
            }
        });
    }

    private void readAndDispatch(long generation) {
        if (!isCurrent(generation)) {
            return;
        }
        final NativeImage pixels;
        try (var image = source.open()) {
            pixels = image.decode();
        } catch (Exception error) {
            throw new CompletionException(error);
        }
        if (!isCurrent(generation)) {
            pixels.close();
            return;
        }
        // 26.1.2 的 queueFencedTask 会立即创建 GL fence，必须在渲染线程调用；
        // 此处还在 worker 线程，先跳到渲染线程再入队（RenderSystem 已无 recordRenderCall）
        try {
            if (RenderSystem.isOnRenderThread()) {
                RenderSystem.queueFencedTask(() -> upload(generation, pixels));
            } else {
                Minecraft.getInstance().execute(() -> {
                    if (isCurrent(generation)) {
                        RenderSystem.queueFencedTask(() -> upload(generation, pixels));
                    } else {
                        pixels.close();
                    }
                });
            }
        } catch (RuntimeException error) {
            pixels.close();
            throw error;
        }
    }

    private void upload(long generation, NativeImage pixels) {
        try (pixels) {
            if (isCurrent(generation)) {
                try {
                    doUpload(pixels);
                } catch (RuntimeException error) {
                    if (fail(generation, error)) {
                        YesSteveModel.LOGGER.debug("Failed to upload model texture from {}", source, error);
                    }
                }
            }
        }
    }

    private boolean nearestSampler;

    private void doUpload(NativeImage img) {
        GpuDevice device = RenderSystem.getDevice();
        if (this.texture != null) {
            this.texture.close();
        }
        if (this.textureView != null) {
            this.textureView.close();
        }
        this.texture = device.createTexture("YSM Custom Texture", 5, TextureFormat.RGBA8, img.getWidth(), img.getHeight(), 1, 1);
        this.textureView = device.createTextureView(this.texture);
        device.createCommandEncoder().writeToTexture(this.texture, img);
    }

    // 26.1.2 的 AbstractTexture 三个 GPU 句柄都必须由子类初始化；异步解码完成前
    // 先放一个 1x1 占位纹理，避免渲染管线 getTextureView() 抛异常（官方 2.6.5 在构造时同样设置 sampler）
    @Override
    public GpuTexture getTexture() {
        ensureInitialized();
        return super.getTexture();
    }

    @Override
    public GpuTextureView getTextureView() {
        ensureInitialized();
        return super.getTextureView();
    }

    @Override
    public GpuSampler getSampler() {
        ensureInitialized();
        return super.getSampler();
    }

    private void ensureInitialized() {
        // 必须无条件覆盖：AbstractTexture 构造时已经把 sampler 初始化为
        // "mag=NEAREST, min=LINEAR"（带线性过滤），判空写法则永远不会生效——
        // 模型贴图被缩小采样时就会糊掉。旧版（2.6.5）用的是临近取样，这里保持一致。
        if (this.sampler == null || !this.nearestSampler) {
            this.sampler = RenderSystem.getSamplerCache()
                    .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                            FilterMode.NEAREST, FilterMode.NEAREST, false);
            this.nearestSampler = true;
        }
        if (this.textureView == null) {
            GpuDevice device = RenderSystem.getDevice();
            this.texture = device.createTexture("YSM Custom Texture Placeholder", 5,
                    TextureFormat.RGBA8, 1, 1, 1, 1);
            this.textureView = device.createTextureView(this.texture);
            var placeholder = new NativeImage(1, 1, false);
            try {
                placeholder.setPixel(0, 0, 0xFFFFFFFF);
                device.createCommandEncoder().writeToTexture(this.texture, placeholder);
            } finally {
                placeholder.close();
            }
        }
    }

    public Optional<Throwable> failure() {
        return loadState.failure().or(failureGate::failure);
    }

    private boolean isCurrent(long generation) {
        return loadState.isCurrent(generation) && failureGate.failure().isEmpty();
    }

    private boolean fail(long generation, Throwable cause) {
        if (!loadState.fail(generation, cause)) {
            return false;
        }
        failureGate.fail(cause);
        return true;
    }

    @Override
    public void close() {
        var task = loadState.deactivate();
        if (task != null) {
            task.cancel(true);
        }
        super.close();
    }

    private static Throwable unwrap(Throwable error) {
        while ((error instanceof CompletionException || error instanceof java.util.concurrent.ExecutionException)
                && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }
}
