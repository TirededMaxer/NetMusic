package com.github.tartaricacid.netmusic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Game-version-specific GUI entry points. */
public final class ClientPlatform {
    private ClientPlatform() {}
    public static void setScreen(Screen screen) { Minecraft.getInstance().setScreen(screen); }
    public static Screen screen() { return Minecraft.getInstance().screen; }
    public static void overlay(Component text, boolean animate) { Minecraft.getInstance().gui.setOverlayMessage(text, animate); }
    public static void nowPlaying(Component text) { Minecraft.getInstance().gui.setNowPlaying(text); }
}
