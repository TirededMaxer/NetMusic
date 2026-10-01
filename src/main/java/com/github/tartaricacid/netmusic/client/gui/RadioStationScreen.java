package com.github.tartaricacid.netmusic.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public class RadioStationScreen extends Screen {
    private final BigMegaphoneScreen parent;
    private final List<RadioCatalog.Station> stations;
    private String group = "", province = "", city = "", query = "";
    private int page, left, top, rows;
    private EditBox search;
    private List<Entry> entries = List.of();
    private record Entry(String label, Runnable action) {}
    public RadioStationScreen(BigMegaphoneScreen parent) {
        super(Component.translatable("gui.netmusic.big_megaphone.select_station"));
        this.parent = parent; stations = RadioCatalog.load();
    }
    @Override protected void init() {
        left = (width - 300) / 2; top = 28; rows = Math.max(1, (height - 116) / 22);
        rebuild();
    }
    private void navigate(String group, String province, String city) {
        this.group = group; this.province = province; this.city = city; page = 0; query = ""; rebuild();
    }
    private void rebuild() {
        clearWidgets();
        search = new EditBox(font, left, top, 300, 18, Component.translatable("gui.netmusic.radio.search"));
        search.setMaxLength(80); search.setValue(query);
        search.setResponder(value -> { query = value; page = 0; rebuildEntries(); });
        addRenderableWidget(search);
        rebuildEntries();
    }
    private void rebuildEntries() {
        // Keep the focused search field while replacing only the list/buttons.
        boolean focused = search.isFocused();
        clearWidgets(); addRenderableWidget(search); search.setFocused(focused);
        List<Entry> result = new ArrayList<>();
        if (!query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            for (var station : stations) if ((station.name() + station.province() + station.city()).toLowerCase(Locale.ROOT).contains(needle)) result.add(stationEntry(station));
        } else if (group.isEmpty()) {
            result.add(new Entry("总台", () -> navigate("总台", "", "")));
            result.add(new Entry("国内地区广播", () -> navigate("国内", "", "")));
            result.add(new Entry("国外", () -> navigate("国外", "", "")));
        } else if (group.equals("国内") && province.isEmpty()) {
            stations.stream().filter(s -> s.group().equals("国内")).map(RadioCatalog.Station::province).distinct().sorted()
                .forEach(p -> result.add(new Entry(p, () -> navigate("国内", p, ""))));
        } else if (group.equals("国内") && city.isEmpty()) {
            result.add(new Entry(province + "省级广播", () -> navigate("国内", province, "省级")));
            stations.stream().filter(s -> s.group().equals("国内") && s.province().equals(province) && !s.city().equals("省级"))
                .map(RadioCatalog.Station::city).distinct().sorted().forEach(ct -> result.add(new Entry(province + "－" + ct, () -> navigate("国内", province, ct))));
        } else {
            stations.stream().filter(s -> s.group().equals(group))
                .filter(s -> !group.equals("国内") || (s.province().equals(province) && s.city().equals(city)))
                .forEach(s -> result.add(stationEntry(s)));
        }
        entries = result;
        int maxPage = Math.max(0, (entries.size() - 1) / rows); page = Math.min(page, maxPage);
        for (int i = page * rows; i < Math.min(entries.size(), (page + 1) * rows); i++) {
            Entry entry = entries.get(i);
            addRenderableWidget(Button.builder(Component.literal(entry.label()), b -> entry.action().run())
                .pos(left, top + 24 + (i - page * rows) * 22).size(300, 20).build());
        }
        Button previous = Button.builder(Component.literal("◀"), b -> { page--; rebuildEntries(); }).pos(left, height - 30).size(60, 20).build();
        previous.active = page > 0; addRenderableWidget(previous);
        addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.back"), b -> back()).pos(left + 66, height - 30).size(168, 20).build());
        Button next = Button.builder(Component.literal("▶"), b -> { page++; rebuildEntries(); }).pos(left + 240, height - 30).size(60, 20).build();
        next.active = page < maxPage; addRenderableWidget(next);
    }
    private Entry stationEntry(RadioCatalog.Station station) {
        String label = query.isBlank() ? station.name() : station.group() + " / " + station.province() + " / " + station.name();
        return new Entry(label, () -> { parent.applyStation(station.name(), station.url()); minecraft.setScreen(parent); });
    }
    private void back() {
        if (!query.isBlank()) { query = ""; page = 0; rebuild(); }
        else if (!city.isEmpty()) navigate(group, province, "");
        else if (!province.isEmpty()) navigate(group, "", "");
        else if (!group.isEmpty()) navigate("", "", "");
        else onClose();
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        String heading = group.isEmpty() ? title.getString() : group + (province.isEmpty() ? "" : " / " + province) + (city.isEmpty() ? "" : " / " + city);
        graphics.centeredText(font, heading, width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font, (page + 1) + " / " + (Math.max(0, (entries.size() - 1) / rows) + 1), width / 2, height - 43, 0xFFAAAAAA);
    }
}
