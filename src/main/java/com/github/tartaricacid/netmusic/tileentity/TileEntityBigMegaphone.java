package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.playback.WorldPlaybackManager;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import org.jspecify.annotations.Nullable;

public class TileEntityBigMegaphone extends BlockEntity {
    private String streamUrl = "", displayName = "";
    private int volume = 100;
    private boolean broadcasting, registered;
    public TileEntityBigMegaphone(BlockPos pos, BlockState state) { super(InitBlocks.BIG_MEGAPHONE_TE, pos, state); }
    @Override public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("StreamUrl", streamUrl); output.putString("DisplayName", displayName);
        output.putInt("Volume", volume); output.putBoolean("Broadcasting", broadcasting);
    }
    @Override public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        streamUrl = input.getStringOr("StreamUrl", ""); displayName = input.getStringOr("DisplayName", "");
        volume = Math.clamp(input.getIntOr("Volume", 100), 0, 100);
        broadcasting = input.getBooleanOr("Broadcasting", false);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) { return saveWithoutMetadata(provider); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    public String getStreamUrl() { return streamUrl; }
    public String getDisplayName() { return displayName; }
    public int getMaxRange() { return 1024; }
    public int getVolume() { return volume; }
    public boolean isBroadcasting() { return broadcasting; }
    public void setVolume(int volume) { this.volume = Math.clamp(volume, 0, 100); markDirty(); }
    public void syncWorld(String url, String name, boolean enabled) {
        if (streamUrl.equals(url) && displayName.equals(name) && broadcasting == enabled) return;
        streamUrl = url; displayName = name; broadcasting = enabled; markDirty();
    }
    public boolean applyConfig(String url, String name, int volume) {
        if (!(level instanceof ServerLevel sl)) return false;
        WorldPlaybackManager.get(sl.getServer()).configure(this, url.trim(), name.trim(), Math.clamp(volume, 0, 100));
        return true;
    }
    // Commands and the editor control the global switch. Redstone cannot desynchronize relays.
    public void onRedstoneSignalChanged(boolean signal) {}
    public void startBroadcast() { if (level instanceof ServerLevel sl) WorldPlaybackManager.get(sl.getServer()).setRadioEnabled(true); }
    public void stopBroadcast() { if (level instanceof ServerLevel sl) WorldPlaybackManager.get(sl.getServer()).setRadioEnabled(false); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel sl) WorldPlaybackManager.get(sl.getServer()).remove(this);
        super.preRemoveSideEffects(pos, state);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, TileEntityBigMegaphone horn) {
        if (!horn.registered && level instanceof ServerLevel sl) {
            horn.registered = true; WorldPlaybackManager.get(sl.getServer()).register(horn);
        }
    }
    public void markDirty() {
        setChanged();
        if (level != null) { BlockState state = level.getBlockState(worldPosition); level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL); }
    }
}
