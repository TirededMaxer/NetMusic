package com.github.tartaricacid.netmusic.client.command;
import com.github.tartaricacid.netmusic.client.gui.NetMusicControlScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class ClientCommands {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("netmusic").executes(context -> {
                var client = context.getSource().getClient();
                client.execute(() -> client.setScreen(new NetMusicControlScreen()));
                return 1;
            })));
    }
}
