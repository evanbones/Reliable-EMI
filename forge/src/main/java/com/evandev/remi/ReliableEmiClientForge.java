package com.evandev.remi;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.config.ReliableEmiConfigScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

public class ReliableEmiClientForge {
    public static void register() {
        ReliableEmiConfig.load();

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> ReliableEmiConfigScreen.createScreen(parent)));

        MinecraftForge.EVENT_BUS.addListener(ReliableEmiClientForge::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("remi")
                .then(Commands.literal("reload")
                        .executes(ReliableEmiClientForge::executeReload)
                )
                .executes(ReliableEmiClientForge::executeReload)
        );
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        try {
            ReliableEmiConfig.reload();
            context.getSource().sendSuccess(() -> ReliableEmi.text("command.reload.success"), false);
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to reload Reliable EMI configs", e);
            context.getSource().sendFailure(ReliableEmi.text("command.reload.failure", e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
