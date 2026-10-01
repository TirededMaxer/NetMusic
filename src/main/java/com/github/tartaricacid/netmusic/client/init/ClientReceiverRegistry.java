package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientReceiverRegistry {
    public static void register() {
        registerReceiver(com.github.tartaricacid.netmusic.network.message.RadioStateMessage.TYPE,
            (message, context) -> context.client().execute(() -> com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager.handleState(message)));
        registerReceiver(com.github.tartaricacid.netmusic.network.message.MusicStopMessage.TYPE,
            (message, context) -> context.client().execute(com.github.tartaricacid.netmusic.client.audio.MusicPlayManager::stopCurrent));
        registerReceiver(MusicToClientMessage.TYPE, MusicToClientMessage::handle);
        registerReceiver(GetMusicListMessage.TYPE, GetMusicListMessage::handle);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
