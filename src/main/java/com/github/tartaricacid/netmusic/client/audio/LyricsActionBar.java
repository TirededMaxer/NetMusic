package com.github.tartaricacid.netmusic.client.audio;
import com.github.tartaricacid.netmusic.api.lyric.*;
import com.github.tartaricacid.netmusic.client.event.ConfigEvent;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class LyricsActionBar {
    private static Object owner;
    private LyricsActionBar() {}
    public static void show(NetMusicSound sound, LyricRecord lyrics, int tick) {
        if (!MusicPlayManager.isCurrentSound(sound)) return;
        if (!GeneralConfig.LYRICS_ACTION_BAR.get()) { clear(sound); return; }
        String original = LyricLines.current(lyrics.getLyrics(), tick);
        String translated = LyricLines.current(lyrics.getTransLyrics(), tick);
        if (original.isEmpty() && translated.isEmpty()) { clear(sound); return; }
        if (tick % 10 != 0) return;
        Component first = Component.literal(original).withStyle(style -> style.withColor(ConfigEvent.PLAYER_ORIGINAL_COLOR & 0xFFFFFF));
        Component second = Component.literal(translated).withStyle(style -> style.withColor(ConfigEvent.PLAYER_TRANSLATED_COLOR & 0xFFFFFF));
        Component line = original.isEmpty() ? second : translated.isEmpty() ? first :
                Component.translatable("gui.netmusic.lyrics.action_bar", first, second);
        Minecraft.getInstance().gui.setOverlayMessage(line, false);
        owner = sound;
    }
    public static void clear(Object sound) {
        if (owner == sound && owner != null) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.empty(), false);
            owner = null;
        }
    }
}
