package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MusicStopMessage() implements CustomPacketPayload {
    public static final Type<MusicStopMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "music_stop"));
    public static final StreamCodec<ByteBuf, MusicStopMessage> STREAM_CODEC = StreamCodec.unit(new MusicStopMessage());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
