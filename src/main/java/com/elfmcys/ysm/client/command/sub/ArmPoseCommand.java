package com.elfmcys.ysm.client.command.sub;

import com.elfmcys.ysm.client.renderer.ArmPoseState;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 第一人称手臂摆放的实时调参命令。
 *
 * <p>用法：{@code /ysmclient armpose} 查看当前值；{@code /ysmclient armpose <轴> <值>} 设置单个轴。
 * 例如 {@code /ysmclient armpose y -1.2}、{@code /ysmclient armpose pitch 15}。
 */
public class ArmPoseCommand {
    private static final String NAME = "armpose";
    private static final List<String> AXES = List.of("x", "y", "z", "yaw", "pitch");

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(NAME)
                .executes(ArmPoseCommand::print)
                .then(Commands.argument("axis", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            AXES.forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("value", FloatArgumentType.floatArg(-1000.0F, 1000.0F))
                                .executes(ArmPoseCommand::set)));
    }

    private static int print(CommandContext<CommandSourceStack> ctx) {
        return report(ctx, "当前手臂摆放：" + ArmPoseState.describe());
    }

    private static int set(CommandContext<CommandSourceStack> ctx) {
        var axis = StringArgumentType.getString(ctx, "axis");
        float value = FloatArgumentType.getFloat(ctx, "value");
        if (!ArmPoseState.set(axis, value)) {
            return report(ctx, "未知的轴：" + axis + "，可用：" + String.join(" / ", AXES));
        }
        return report(ctx, axis + " = " + value + "，当前：" + ArmPoseState.describe());
    }

    private static int report(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal("[YSM] " + message), false);
        return Command.SINGLE_SUCCESS;
    }
}
