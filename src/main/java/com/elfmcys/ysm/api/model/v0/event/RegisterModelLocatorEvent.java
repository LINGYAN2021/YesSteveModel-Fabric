package com.elfmcys.ysm.api.model.v0.event;

import com.elfmcys.ysm.api.model.v0.ModelKind;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocator;
import com.elfmcys.ysm.event.bus.YsmEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

public class RegisterModelLocatorEvent extends YsmEvent {
    private final ModelKind kind;
    private final Function<String, GeoLocator> registerFunction;

    public RegisterModelLocatorEvent(ModelKind kind, Function<String, GeoLocator> registerFunction) {
        this.kind = kind;
        this.registerFunction = registerFunction;
    }

    public ModelKind kind() {
        return kind;
    }

    public GeoLocator register(String locatorName) {
        return registerFunction.apply(locatorName);
    }
}
