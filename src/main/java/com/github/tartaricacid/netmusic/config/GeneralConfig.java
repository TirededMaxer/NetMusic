package com.github.tartaricacid.netmusic.config;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig {
    public static ModConfigSpec.BooleanValue ENABLE_PLAYER_LYRICS, LYRICS_ACTION_BAR;
    public static ModConfigSpec.ConfigValue<String> ORIGINAL_PLAYER_LYRICS_COLOR, TRANSLATED_PLAYER_LYRICS_COLOR;
    public static ModConfigSpec init() {
        var builder = new ModConfigSpec.Builder();
        builder.push("general");
        ENABLE_PLAYER_LYRICS = builder.comment("Display lyrics above the music player").define("EnablePlayerLyrics", true);
        LYRICS_ACTION_BAR = builder.comment("Display current lyrics in the ActionBar").define("LyricsActionBar", false);
        ORIGINAL_PLAYER_LYRICS_COLOR = builder.comment("Original lyrics color in #ARGB format").define("OriginalPlayerLyricsColor", "#FFAAAAAA");
        TRANSLATED_PLAYER_LYRICS_COLOR = builder.comment("Translated lyrics color in #ARGB format").define("TranslatedPlayerLyricsColor", "#FFFFFFFF");
        builder.pop();
        return builder.build();
    }
    public static void save() {
        ENABLE_PLAYER_LYRICS.save(); LYRICS_ACTION_BAR.save();
        ORIGINAL_PLAYER_LYRICS_COLOR.save(); TRANSLATED_PLAYER_LYRICS_COLOR.save();
    }
}
