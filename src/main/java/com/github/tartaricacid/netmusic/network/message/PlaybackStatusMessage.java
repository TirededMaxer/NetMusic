package com.github.tartaricacid.netmusic.network.message;
import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlaybackStatusMessage(boolean musicEnabled, boolean radioEnabled, int error) implements CustomPacketPayload {
    public static final Type<PlaybackStatusMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "playback_status"));
    public static final StreamCodec<ByteBuf, PlaybackStatusMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlaybackStatusMessage::musicEnabled,
            ByteBufCodecs.BOOL, PlaybackStatusMessage::radioEnabled,
            ByteBufCodecs.VAR_INT, PlaybackStatusMessage::error, PlaybackStatusMessage::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
