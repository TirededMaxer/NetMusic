package com.github.tartaricacid.netmusic.api.lyric;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LyricLinesTest {
    @Test void actionBarNeverShowsFutureLines() {
        SortedMap<Integer,String> lines = new TreeMap<>(Map.of(20,"first",40,"second",60,""));
        assertEquals("", LyricLines.current(lines, 0));
        assertEquals("first", LyricLines.current(lines, 20));
        assertEquals("first", LyricLines.current(lines, 39));
        assertEquals("second", LyricLines.current(lines, 40));
        assertEquals("", LyricLines.current(lines, 60));
    }
    @Test void delayedTranslationHasItsOwnTimeline() {
        SortedMap<Integer,String> original = new TreeMap<>(Map.of(0,"one",20,"two"));
        SortedMap<Integer,String> translated = new TreeMap<>(Map.of(10,"translation one",30,"translation two"));
        assertEquals("two", LyricLines.current(original, 25));
        assertEquals("translation one", LyricLines.current(translated, 25));
    }
    @Test void missingLinesAreEmpty() {
        assertEquals("", LyricLines.current(null, 100));
        assertEquals("", LyricLines.current(new TreeMap<>(), 100));
        SortedMap<Integer,String> lines = new TreeMap<>(); lines.put(0, null);
        assertEquals("", LyricLines.current(lines, 100));
    }
}
