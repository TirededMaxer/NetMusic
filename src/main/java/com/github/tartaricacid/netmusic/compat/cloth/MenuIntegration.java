package com.github.tartaricacid.netmusic.compat.cloth;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.client.event.ConfigEvent;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;



public class MenuIntegration {
    public static Screen getModsConfigScreen(Screen parent) {
        return getConfigBuilder().setParentScreen(parent).build();
    }

    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder root = ConfigBuilder.create().setTitle(Component.translatable("itemGroup.netmusic"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();
        generalConfig(root, entryBuilder);
        root.setSavingRunnable(() -> {
            GeneralConfig.save();
            ConfigEvent.reloadColors();
        });
        return root;
    }

    @SuppressWarnings("all")
    private static void generalConfig(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory general = root.getOrCreateCategory(Component.translatable("config.netmusic.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.netmusic.general.enable_player_lyrics"), GeneralConfig.ENABLE_PLAYER_LYRICS.get())
                .setTooltip(Component.translatable("config.netmusic.general.enable_player_lyrics.tooltip"))
                .setDefaultValue(GeneralConfig.ENABLE_PLAYER_LYRICS.getDefault())
                .setSaveConsumer(GeneralConfig.ENABLE_PLAYER_LYRICS::set)
                .build());


        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.netmusic.general.lyrics_action_bar"), GeneralConfig.LYRICS_ACTION_BAR.get())
                .setTooltip(Component.translatable("config.netmusic.general.lyrics_action_bar.tooltip"))
                .setDefaultValue(GeneralConfig.LYRICS_ACTION_BAR.getDefault()).setSaveConsumer(GeneralConfig.LYRICS_ACTION_BAR::set).build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.original_player_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.original_player_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.translated_player_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.translated_player_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());



    }
}

