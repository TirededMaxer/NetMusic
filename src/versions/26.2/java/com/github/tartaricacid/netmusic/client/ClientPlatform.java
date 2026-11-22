package com.github.tartaricacid.netmusic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Minecraft 26.2 separates screen management and the HUD. */
public final class ClientPlatform {
    private ClientPlatform() {}
    public static void setScreen(Screen screen) { Minecraft.getInstance().gui.setScreen(screen); }
    public static Screen screen() { return Minecraft.getInstance().gui.screen(); }
    public static void overlay(Component text, boolean animate) { Minecraft.getInstance().gui.hud.setOverlayMessage(text, animate); }
    public static void deferSubtitles() { Minecraft.getInstance().gui.hud.extractDeferredSubtitles(); }
    public static void nowPlaying(Component text) { Minecraft.getInstance().gui.hud.setNowPlaying(text); }
}
