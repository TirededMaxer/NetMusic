package com.github.tartaricacid.netmusic.network.message;
import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.playback.WorldPlaybackManager;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlaybackControlMessage(int action) implements CustomPacketPayload {
    public static final int QUERY = 0, MUSIC = 1, RADIO = 2;
    public static final Type<PlaybackControlMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "playback_control"));
    public static final StreamCodec<ByteBuf, PlaybackControlMessage> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(PlaybackControlMessage::new, PlaybackControlMessage::action);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(PlaybackControlMessage message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            var manager = WorldPlaybackManager.get(context.server());
            int error = 0;
            if (message.action == MUSIC) {
                if (!manager.setMusicEnabled(!manager.musicEnabled(), context.player())) error = 1;
            } else if (message.action == RADIO) {
                if (!manager.setRadioEnabled(!manager.radioEnabled())) error = 2;
            } else if (message.action != QUERY) return;
            NetworkHandler.sendToClientPlayer(new PlaybackStatusMessage(manager.musicEnabled(), manager.radioEnabled(), error), context.player());
        });
    }
}
