package com.github.tartaricacid.netmusic.radio;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** The public web directory used by https://www.radio.cn/pc-portal/erji/radioStation.html. */
public final class RadioDirectoryApi {
    private static final String BASE = "https://ytmsout.radio.cn";
    private static final String WEB_KEY = "f0fc4c668392f9f9a447e48584c214ee";
    private RadioDirectoryApi() {}
    public static JsonArray request(String path, Map<String, String> params) throws IOException {
        try {
            String query = String.join("&", new TreeMap<>(params).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList());
            String timestamp = Long.toString(System.currentTimeMillis());
            String text = (query.isEmpty() ? "" : query + "&") + "timestamp=" + timestamp + "&key=" + WEB_KEY;
            String sign = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8))).toUpperCase(Locale.ROOT);
            JsonObject response = JsonParser.parseString(NetWorker.get(BASE + path + (query.isEmpty() ? "" : "?" + query),
                Map.of("timestamp", timestamp, "sign", sign, "equipmentId", "0000", "platformCode", "WEB", "Content-Type", "application/json"))).getAsJsonObject();
            if (response.get("code").getAsInt() != 0 || !response.get("data").isJsonArray()) throw new IOException("Radio directory rejected request");
            return response.getAsJsonArray("data");
        } catch (IOException e) { throw e; } catch (Exception e) { throw new IOException("Cannot read radio directory", e); }
    }
    public static String stationUrl(String province, String id) {
        return "https://www.radio.cn/pc-portal/erji/radioStation.html?provinceCode=" + province + "&id=" + id;
    }
    public static boolean isStationUrl(String url) {
        try { URI uri = URI.create(url); return "https".equals(uri.getScheme()) && "www.radio.cn".equals(uri.getHost()) && "/pc-portal/erji/radioStation.html".equals(uri.getPath()) && uri.getQuery() != null; }
        catch (Exception e) { return false; }
    }
    public static URI resolve(String url) throws IOException {
        Map<String, String> params = new HashMap<>();
        String query = URI.create(url).getRawQuery();
        if (query == null) throw new IOException("Missing station ID");
        for (String entry : query.split("&")) { String[] pair = entry.split("=", 2); if (pair.length == 2) params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8)); }
        String province = params.getOrDefault("provinceCode", "0"), id = params.getOrDefault("id", "");
        if (!province.matches("[0-9]+") || !id.matches("[0-9]+")) throw new IOException("Invalid station ID");
        for (JsonElement element : request("/web/appBroadcast/list", Map.of("categoryId", "0", "provinceCode", province))) {
            JsonObject station = element.getAsJsonObject();
            if (!id.equals(station.get("contentId").getAsString())) continue;
            for (String key : new String[] {"playUrlLow", "playUrlMulti", "mp3PlayUrlHigh", "mp3PlayUrlLow"}) {
                if (station.has(key) && !station.get(key).isJsonNull()) {
                    String stream = station.get(key).getAsString();
                    if (stream.startsWith("http://") || stream.startsWith("https://")) return URI.create(stream);
                }
            }
        }
        throw new IOException("Station is unavailable in the official directory");
    }
}
