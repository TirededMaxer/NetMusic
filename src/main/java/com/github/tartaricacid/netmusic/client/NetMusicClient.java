package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.client.init.InitContainerGui;
import com.github.tartaricacid.netmusic.client.init.InitEvents;
import com.github.tartaricacid.netmusic.client.init.InitModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class NetMusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.github.tartaricacid.netmusic.client.command.ClientCommands.init();

        InitContainerGui.init();
        InitModel.init();
        InitEvents.init();
        ClientReceiverRegistry.register();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.stopCurrent();
            com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager.clearAll();
        });
    }
}