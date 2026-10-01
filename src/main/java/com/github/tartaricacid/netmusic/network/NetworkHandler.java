package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.network.message.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NetworkHandler {
    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(PlaybackControlMessage.TYPE, PlaybackControlMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlaybackStatusMessage.TYPE, PlaybackStatusMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RadioStateMessage.TYPE, RadioStateMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MusicStopMessage.TYPE, MusicStopMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MusicToClientMessage.TYPE, MusicToClientMessage.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetMusicIDMessage.TYPE, SetMusicIDMessage.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(BigMegaphoneControlMessage.TYPE, BigMegaphoneControlMessage.STREAM_CODEC);
    }

    public static void broadcast(net.minecraft.server.MinecraftServer server, CustomPacketPayload message) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, message));
    }

    public static void sendToNearBy(Level world, BlockPos pos, CustomPacketPayload message) {
        if (world instanceof ServerLevel serverWorld) {
            PlayerLookup.around(serverWorld, pos, 1024)
                    .forEach(p -> ServerPlayNetworking.send(p, message));
        }
    }

    public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }
}
