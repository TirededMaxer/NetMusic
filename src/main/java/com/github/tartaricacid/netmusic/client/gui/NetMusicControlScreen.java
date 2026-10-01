package com.github.tartaricacid.netmusic.client.gui;
import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import com.github.tartaricacid.netmusic.network.message.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NetMusicControlScreen extends Screen {
    private Button music, radio;
    private PlaybackStatusMessage status;
    private Component feedback = Component.empty();
    private int ticks;
    public NetMusicControlScreen() { super(Component.translatable("gui.netmusic.control.title")); }
    @Override protected void init() {
        int left = (width - 220) / 2, top = height / 2 - 36;
        addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.control.config"),
                b -> minecraft.setScreen(MenuIntegration.getModsConfigScreen(this))).pos(left, top).size(220, 20).build());
        music = addRenderableWidget(Button.builder(label(true), b -> send(PlaybackControlMessage.MUSIC))
                .pos(left, top + 26).size(220, 20).build());
        radio = addRenderableWidget(Button.builder(label(false), b -> send(PlaybackControlMessage.RADIO))
                .pos(left, top + 52).size(220, 20).build());
        updateButtons(); send(PlaybackControlMessage.QUERY);
    }
    private Component label(boolean isMusic) {
        String key = status == null ? "gui.netmusic.control.loading" :
                (isMusic ? status.musicEnabled() : status.radioEnabled()) ? "gui.netmusic.control.on" : "gui.netmusic.control.off";
        return Component.translatable(isMusic ? "gui.netmusic.control.music" : "gui.netmusic.control.radio", Component.translatable(key));
    }
    private void updateButtons() {
        if (music == null || radio == null) return;
        boolean ready = status != null && ClientPlayNetworking.canSend(PlaybackControlMessage.TYPE);
        music.active = radio.active = ready; music.setMessage(label(true)); radio.setMessage(label(false));
    }
    private void send(int action) {
        if (!ClientPlayNetworking.canSend(PlaybackControlMessage.TYPE)) {
            feedback = Component.translatable("gui.netmusic.control.server_missing"); updateButtons(); return;
        }
        if (action != PlaybackControlMessage.QUERY) {
            music.active = radio.active = false; feedback = Component.empty();
        }
        ClientPlayNetworking.send(new PlaybackControlMessage(action));
    }
    public void apply(PlaybackStatusMessage next) {
        status = next;
        if (next.error() != 0) feedback = Component.translatable(next.error() == 1 ? "gui.netmusic.control.no_disc" : "gui.netmusic.control.no_station");
        updateButtons();
    }
    @Override public void tick() { if (++ticks % 20 == 0) send(PlaybackControlMessage.QUERY); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, height / 2 - 60, 0xFFFFFFFF);
        graphics.centeredText(font, feedback, width / 2, height / 2 + 46, 0xFFFF5555);
    }
}
