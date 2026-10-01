package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.api.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import java.net.*;
import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class MusicPlayManager {
    public static final String ERROR_404 = "http://music.163.com/404";
    public static final String MUSIC_163_URL = "https://music.163.com/";
    private static SoundInstance current;
    private static volatile long generation;
    public static long beginRequest() { stopCurrent(); return generation; }
    public static boolean isCurrentSound(SoundInstance sound) { return current == sound; }
    public static boolean isCurrent(long request) { return request == generation; }
    public static void stopCurrent() {
        generation++;
        LyricsActionBar.clear(current);
        if (current != null) Minecraft.getInstance().getSoundManager().stop(current);
        current = null;
    }
    public static void play(long request, String url, String name, Function<URL, SoundInstance> factory) {
        Optional<String> source = getFinalUrl(url);
        if (source.isEmpty()) return;
        try {
            URL parsed = URI.create(source.get()).toURL();
            Minecraft.getInstance().execute(() -> {
                if (!isCurrent(request) || Minecraft.getInstance().level == null) return;
                if (current != null) Minecraft.getInstance().getSoundManager().stop(current);
                current = factory.apply(parsed);
                Minecraft.getInstance().getSoundManager().play(current);
                Minecraft.getInstance().gui.setNowPlaying(Component.literal(name));
            });
        } catch (Exception e) { NetMusic.LOGGER.error("Malformed music URL", e); }
    }
    public static Optional<String> getFinalUrl(String url) {
        try {
            URI source = URI.create(url);
            if ((source.getScheme().equalsIgnoreCase("http") || source.getScheme().equalsIgnoreCase("https")) && source.getHost() != null)
                return Optional.of(url);
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}
