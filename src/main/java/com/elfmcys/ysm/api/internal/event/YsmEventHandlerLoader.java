package com.elfmcys.ysm.api.internal.event;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.annotation.YsmEventHandler;
import com.elfmcys.ysm.event.bus.YsmEventBus;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric 侧的 {@link YsmEventHandler} 发现与注册桥。
 * 外部模组通过 fabric.mod.json 的 "ysm:event_handler" entrypoint 提供处理器实例。
 */
public final class YsmEventHandlerLoader {
    public static final String ENTRYPOINT = "ysm:event_handler";

    private YsmEventHandlerLoader() {
    }

    public static void attach() {
        for (Object handler : FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT, Object.class)
                .stream().map(container -> container.getEntrypoint()).toList()) {
            registerOne(handler);
        }
    }

    private static void registerOne(Object handler) {
        String handlerName = handler.getClass().getName();
        YsmEventHandlerRegistration.Outcome outcome;
        try {
            outcome = YsmEventHandlerRegistration.prepareChecked(handler);
        } catch (YsmEventHandlerRegistration.Failure failure) {
            if (failure.stage() == YsmEventHandlerRegistration.Stage.CHECKER_MISSING) {
                YesSteveModel.LOGGER.error(
                        "Skipping @YsmEventHandler {} because checker {} is missing. "
                                + "Add the classifierless YSM JAR to annotationProcessor.",
                        handlerName, failure.subject());
            } else {
                YesSteveModel.LOGGER.error(
                        "Skipping @YsmEventHandler {}: {} ({})",
                        handlerName, failure.getMessage(), failure.subject(), failure.getCause());
            }
            return;
        }

        if (!outcome.shouldRegister()) {
            YesSteveModel.LOGGER.warn(
                    "Skipping @YsmEventHandler {}: status={}, coverageComplete={}, issues={}",
                    handlerName, outcome.status(), outcome.coverageComplete(), outcome.issues());
            return;
        }
        if (outcome.hasWarnings()) {
            YesSteveModel.LOGGER.warn(
                    "Registering @YsmEventHandler {} with compatibility warnings: "
                            + "status={}, coverageComplete={}, issues={}",
                    handlerName, outcome.status(), outcome.coverageComplete(), outcome.issues());
        }

        try {
            YsmEventBus.registerHandler(outcome.handler());
            YesSteveModel.LOGGER.info("Registered @YsmEventHandler {}", handlerName);
        } catch (RuntimeException | LinkageError exception) {
            YesSteveModel.LOGGER.error(
                    "Failed to register @YsmEventHandler {}; continuing without it",
                    handlerName, exception);
        }
    }
}
