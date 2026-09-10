package com.elfmcys.ysm.config;

import com.elfmcys.ysm.config.spec.YsmConfigSpec;

public class ExtraPlayerScreenConfig {
    public static YsmConfigSpec.BooleanValue DISABLE_PLAYER_RENDER;
    public static YsmConfigSpec.IntValue PLAYER_POS_X;
    public static YsmConfigSpec.IntValue PLAYER_POS_Y;
    public static YsmConfigSpec.DoubleValue PLAYER_SCALE;
    public static YsmConfigSpec.DoubleValue PLAYER_YAW_OFFSET;

    public static void init(YsmConfigSpec.Builder builder) {
        builder.push("extra_player_render");

        builder.comment("Whether to display player");
        DISABLE_PLAYER_RENDER = builder.define("DisablePlayerRender", false);

        builder.comment("Player position x in screen");
        PLAYER_POS_X = builder.defineInRange("PlayerPosX", 10, 0, Integer.MAX_VALUE);

        builder.comment("Player position y in screen");
        PLAYER_POS_Y = builder.defineInRange("PlayerPosY", 10, 0, Integer.MAX_VALUE);

        builder.comment("Player scale in screen");
        PLAYER_SCALE = builder.defineInRange("PlayerScale", 40, 8.0, 360.0);

        builder.comment("Player yaw offset in screen");
        PLAYER_YAW_OFFSET = builder.defineInRange("PlayerYawOffset", 5, Double.MIN_VALUE, Double.MAX_VALUE);

        builder.pop();
    }
}