package com.github.tartaricacid.netmusic.network.message;
import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.network.client.MusicToClientMessageClient;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import java.util.concurrent.CompletableFuture;

public record MusicToClientMessage(BlockPos pos, String url, String rawUrl, int timeSecond, String songName, int elapsedTicks) implements CustomPacketPayload {
    public static final Type<MusicToClientMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "music_to_client"));
    public static final StreamCodec<ByteBuf, MusicToClientMessage> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, MusicToClientMessage::pos, ByteBufCodecs.STRING_UTF8, MusicToClientMessage::url,
        ByteBufCodecs.STRING_UTF8, MusicToClientMessage::rawUrl, ByteBufCodecs.VAR_INT, MusicToClientMessage::timeSecond,
        ByteBufCodecs.STRING_UTF8, MusicToClientMessage::songName, ByteBufCodecs.VAR_INT, MusicToClientMessage::elapsedTicks, MusicToClientMessage::new);
    public static void handle(MusicToClientMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            long request = MusicPlayManager.beginRequest();
            CompletableFuture.runAsync(() -> MusicToClientMessageClient.onHandle(message, request), Util.backgroundExecutor());
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
