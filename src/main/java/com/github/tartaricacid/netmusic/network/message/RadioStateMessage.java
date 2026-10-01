package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RadioStateMessage(long session, String url, String name, BlockPos pos, float gain) implements CustomPacketPayload {
    public static final Type<RadioStateMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "radio_state"));
    public static final StreamCodec<ByteBuf, RadioStateMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, RadioStateMessage::session, ByteBufCodecs.STRING_UTF8, RadioStateMessage::url,
        ByteBufCodecs.STRING_UTF8, RadioStateMessage::name, BlockPos.STREAM_CODEC, RadioStateMessage::pos,
        ByteBufCodecs.FLOAT, RadioStateMessage::gain, RadioStateMessage::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
