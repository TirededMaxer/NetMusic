package com.github.tartaricacid.netmusic.api.lyric;
import java.util.SortedMap;
public final class LyricLines {
    private LyricLines() {}
    public static String current(SortedMap<Integer, String> lines, int tick) {
        String text = "";
        if (lines != null) for (var entry : lines.entrySet()) {
            if (entry.getKey() > tick) break;
            text = entry.getValue() == null ? "" : entry.getValue().strip();
        }
        return text;
    }
}
