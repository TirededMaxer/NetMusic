package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.message.RadioStateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import java.net.URI;

/** One connection per client. Moving between relay coverage does not reopen the stream. */
public final class BigMegaphoneClientManager {
    private static RadioStateMessage state;
    private static BigMegaphoneSound sound;
    private static ClientLevel level;
    private static long retryAt;
    private static int failures;
    private BigMegaphoneClientManager() {}
    public static void handleState(RadioStateMessage next) {
        Minecraft mc = Minecraft.getInstance();
        if (level != mc.level) { clearAll(); level = mc.level; }
        if (state != null && next.session() < state.session()) return;
        boolean changed = state == null || state.session() != next.session() || !state.url().equals(next.url());
        if (changed) { stop(); failures = 0; retryAt = 0; }
        state = next;
        if (next.url().isBlank() || next.gain() <= 0) { stop(); return; }
        if (sound != null) sound.updateEmitter(next.pos(), next.gain());
        startIfNeeded();
    }
    private static void stop() {
        if (sound != null) { sound.forceStop(); Minecraft.getInstance().getSoundManager().stop(sound); sound = null; }
    }
    private static void startIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (state == null || state.url().isBlank() || state.gain() <= 0 || sound != null || failures >= 5 || mc.level == null || mc.level.getGameTime() < retryAt) return;
        try {
            sound = new BigMegaphoneSound(state.pos(), state.session(), URI.create(state.url()).toURL(), state.gain());
            mc.getSoundManager().play(sound);
        } catch (Exception error) { NetMusic.LOGGER.error("Invalid radio URL", error); failures = 5; }
    }
    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level || mc.player == null) { clearAll(); level = mc.level; return; }
        if (sound != null && sound.isStopped()) { sound = null; retryAt = mc.level.getGameTime() + 40; }
        startIfNeeded();
    }
    public static void clearAll() { stop(); state = null; failures = 0; retryAt = 0; level = null; }
    public static void handleStreamOpenSuccess(net.minecraft.core.BlockPos pos, long session, BigMegaphoneSound instance) {
        if (sound == instance) failures = 0;
    }
    public static void handleStreamOpenFailure(net.minecraft.core.BlockPos pos, long session, BigMegaphoneSound instance, Exception error) {
        if (sound != instance) return;
        stop(); failures++;
        Minecraft mc = Minecraft.getInstance();
        retryAt = mc.level == null ? 0 : mc.level.getGameTime() + (40L << Math.min(failures, 5));
        NetMusic.LOGGER.warn("Radio stream failed; retry attempt {}", failures, error);
    }
}
