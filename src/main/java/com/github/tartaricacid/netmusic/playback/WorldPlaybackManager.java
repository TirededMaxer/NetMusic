package com.github.tartaricacid.netmusic.playback;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.RadioStateMessage;
import com.github.tartaricacid.netmusic.network.message.MusicStopMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

public final class WorldPlaybackManager {
    private static final Map<MinecraftServer, WorldPlaybackManager> WORLDS = new WeakHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final MinecraftServer server;
    private final PlaybackLease<TileEntityMusicPlayer> lease = new PlaybackLease<>();
    private final Set<TileEntityMusicPlayer> players = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<TileEntityBigMegaphone> loadedHorns = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<UUID, RadioStateMessage> lastRadio = new HashMap<>();
    private State state;
    private com.github.tartaricacid.netmusic.network.message.MusicToClientMessage song;
    private int songStarted;
    private final Set<UUID> musicAudience = new HashSet<>();
    private long radioSession = System.currentTimeMillis();

    private WorldPlaybackManager(MinecraftServer server) {
        this.server = server;
        this.state = new State();
        if (Files.isRegularFile(path())) {
            try (var reader = Files.newBufferedReader(path(), StandardCharsets.UTF_8)) {
                State saved = GSON.fromJson(reader, State.class);
                if (saved != null && saved.horns != null && saved.url != null && saved.name != null) state = saved;
            } catch (Exception e) { NetMusic.LOGGER.error("Cannot read world radio state", e); }
        }
    }
    public static WorldPlaybackManager get(MinecraftServer server) { return WORLDS.computeIfAbsent(server, WorldPlaybackManager::new); }
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> get(server).tick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { WorldPlaybackManager manager = WORLDS.remove(server); if (manager != null) manager.save(); });
    }
    private Path path() { return server.getWorldPath(LevelResource.ROOT).resolve("netmusic-world.json"); }
    private void save() {
        try {
            Path temp = path().resolveSibling("netmusic-world.json.tmp");
            Files.writeString(temp, GSON.toJson(state), StandardCharsets.UTF_8);
            try { Files.move(temp, path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, path(), StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { NetMusic.LOGGER.error("Cannot save world radio state", e); }
    }
    public void register(TileEntityMusicPlayer player) { players.add(player); }
    public boolean musicEnabled() { return state.musicEnabled; }
    public long claim(TileEntityMusicPlayer player) {
        TileEntityMusicPlayer previous = lease.owner();
        lease.clear();
        song = null; musicAudience.clear();
        if (previous != null) previous.stopPlayback();
        NetworkHandler.broadcast(server, new MusicStopMessage());
        return lease.claim(player);
    }
    public void publish(TileEntityMusicPlayer player, long token, com.github.tartaricacid.netmusic.network.message.MusicToClientMessage message) {
        if (!owns(player, token)) return;
        song = message; songStarted = server.getTickCount(); musicAudience.clear(); updateMusicAudience();
    }
    private void updateMusicAudience() {
        var owner = lease.owner();
        if (owner != null && (owner.isRemoved() || !owner.isPlay()) && song != null) { release(owner); return; }
        if (song == null || owner == null) return;
        int elapsed = Math.max(0, server.getTickCount() - songStarted);
        Set<UUID> current = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean audible = owner.getLevel() == player.level() &&
                player.distanceToSqr(Vec3.atCenterOf(owner.getBlockPos())) < (double) PlaybackRules.RANGE * PlaybackRules.RANGE;
            if (audible) {
                current.add(player.getUUID());
                if (musicAudience.add(player.getUUID()))
                    NetworkHandler.sendToClientPlayer(new com.github.tartaricacid.netmusic.network.message.MusicToClientMessage(
                        song.pos(), song.url(), song.rawUrl(), song.timeSecond(), song.songName(), elapsed), player);
            } else if (musicAudience.remove(player.getUUID())) NetworkHandler.sendToClientPlayer(new MusicStopMessage(), player);
        }
        musicAudience.retainAll(current);
    }
    public boolean owns(TileEntityMusicPlayer player, long token) { return state.musicEnabled && lease.owns(player, token); }
    public void release(TileEntityMusicPlayer player) {
        if (lease.owner() == player) {
            lease.release(player);
            song = null; musicAudience.clear();
            NetworkHandler.broadcast(server, new MusicStopMessage());
        }
    }
    public void remove(TileEntityMusicPlayer player) { release(player); players.remove(player); }
    public boolean setMusicEnabled(boolean enabled, ServerPlayer sender) {
        state.musicEnabled = enabled;
        if (!enabled) {
            TileEntityMusicPlayer owner = lease.owner();
            lease.clear();
            song = null; musicAudience.clear();
            if (owner != null) owner.stopPlayback();
            NetworkHandler.broadcast(server, new MusicStopMessage());
        } else if (lease.owner() == null) {
            players.removeIf(p -> p.isRemoved() || p.getLevel() == null);
            players.stream().filter(p -> ItemMusicCD.getSongInfo(p.getItem(0)) != null)
                .filter(p -> sender == null || p.getLevel() == sender.level())
                .min(Comparator.comparingDouble(p -> sender == null ? 0 : sender.distanceToSqr(Vec3.atCenterOf(p.getBlockPos()))))
                .ifPresent(p -> p.setPlayToClient(ItemMusicCD.getSongInfo(p.getItem(0))));
        }
        save();
        return !enabled || lease.owner() != null;
    }
    public void register(TileEntityBigMegaphone horn) {
        loadedHorns.add(horn);
        String dimension = dimension(horn.getLevel());
        long pos = horn.getBlockPos().asLong();
        if (state.horns.stream().noneMatch(h -> h.dimension.equals(dimension) && h.pos == pos)) {
            state.horns.add(new Horn(dimension, pos, horn.getVolume()));
            save();
        }
        horn.syncWorld(state.url, state.name, state.radioEnabled);
    }
    public void remove(TileEntityBigMegaphone horn) {
        loadedHorns.remove(horn);
        String dimension = dimension(horn.getLevel());
        state.horns.removeIf(h -> h.dimension.equals(dimension) && h.pos == horn.getBlockPos().asLong());
        save(); tick();
    }
    public void configure(TileEntityBigMegaphone horn, String url, String name, int volume) {
        boolean stationChanged = !state.url.equals(url) || !state.name.equals(name);
        state.url = url; state.name = name;
        String dimension = dimension(horn.getLevel());
        state.horns.removeIf(h -> h.dimension.equals(dimension) && h.pos == horn.getBlockPos().asLong());
        state.horns.add(new Horn(dimension, horn.getBlockPos().asLong(), volume));
        horn.setVolume(volume);
        if (stationChanged) radioSession++;
        syncHorns(); save(); tick();
    }
    public boolean setRadioEnabled(boolean enabled) {
        if (enabled && (state.url.isBlank() || state.name.isBlank())) return false;
        if (state.radioEnabled != enabled) radioSession++;
        state.radioEnabled = enabled;
        syncHorns(); save(); tick();
        return true;
    }
    private void syncHorns() {
        loadedHorns.removeIf(h -> h.isRemoved() || h.getLevel() == null);
        for (var horn : loadedHorns) horn.syncWorld(state.url, state.name, state.radioEnabled);
    }
    private static String dimension(net.minecraft.world.level.Level level) { return level.dimension().identifier().toString(); }
    private void tick() {
        if (server.getTickCount() % 10 != 0 && !lastRadio.isEmpty()) return;
        updateMusicAudience();
        Set<UUID> connected = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            connected.add(player.getUUID());
            Horn best = null; float bestGain = 0;
            if (state.radioEnabled) {
                for (Horn horn : state.horns) {
                    if (!horn.dimension.equals(dimension(player.level()))) continue;
                    BlockPos pos = BlockPos.of(horn.pos);
                    // Validate when the chunk is available without loading it merely for audio.
                    if (player.level().hasChunkAt(pos) && !(player.level().getBlockEntity(pos) instanceof TileEntityBigMegaphone)) continue;
                    float gain = PlaybackRules.gain(player.distanceToSqr(Vec3.atCenterOf(pos)), horn.volume);
                    if (gain > bestGain) { best = horn; bestGain = gain; }
                }
            }
            RadioStateMessage message = new RadioStateMessage(radioSession, best == null ? "" : state.url,
                state.name, best == null ? player.blockPosition() : BlockPos.of(best.pos), bestGain);
            if (!message.equals(lastRadio.put(player.getUUID(), message))) NetworkHandler.sendToClientPlayer(message, player);
        }
        lastRadio.keySet().retainAll(connected);
    }
    public static final class State {
        public String url = "", name = "";
        public boolean radioEnabled, musicEnabled = true;
        public List<Horn> horns = new ArrayList<>();
    }
    public record Horn(String dimension, long pos, int volume) {}
}
