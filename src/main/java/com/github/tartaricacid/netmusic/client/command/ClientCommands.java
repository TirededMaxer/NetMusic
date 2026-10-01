package com.github.tartaricacid.netmusic.client.command;

import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ClientCommands {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("netmusic").then(literal("config").executes(context -> {
                var client = context.getSource().getClient();
                client.execute(() -> client.setScreen(MenuIntegration.getModsConfigScreen(client.screen)));
                return 1;
            }))));
    }
}
