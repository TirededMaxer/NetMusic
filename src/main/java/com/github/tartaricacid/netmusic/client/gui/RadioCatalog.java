package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class RadioCatalog {
    private RadioCatalog() {}
    public record Station(String name, String url, String group, String province, String city) {}
    public static List<Station> load() {
        try (var reader = new InputStreamReader(Minecraft.getInstance().getResourceManager().open(
                Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "radio_stations.json")), StandardCharsets.UTF_8)) {
            Station[] stations = new Gson().fromJson(reader, Station[].class);
            return stations == null ? List.of() : List.of(stations);
        } catch (Exception e) { NetMusic.LOGGER.error("Cannot read radio directory", e); return List.of(); }
    }
}
