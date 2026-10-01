package com.github.tartaricacid.netmusic.client.api.implement;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
class HlsPlaylistTest {
    @Test void resolvesRelativeVariantWithCrLf() throws Exception {
        assertEquals(URI.create("https://radio.example/live/audio.m3u8"),
            HlsPlaylist.variant(URI.create("https://radio.example/live/master.m3u8"),
                "#EXTM3U\r\n#EXT-X-STREAM-INF:BANDWIDTH=64000\r\n#comment\r\naudio.m3u8\r\n"));
    }
    @Test void preservesSignedAbsoluteVariant() throws Exception {
        assertEquals(URI.create("https://cdn.example/audio.m3u8?token=123"),
            HlsPlaylist.variant(URI.create("https://radio.example/master.m3u8"),
                "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=64000\nhttps://cdn.example/audio.m3u8?token=123\n"));
    }
    @Test void mediaPlaylistHasNoVariant() throws Exception {
        assertNull(HlsPlaylist.variant(URI.create("https://radio.example/live.m3u8"),
            "#EXTM3U\n#EXTINF:10,\nsegment.aac\n"));
    }
    @Test void rejectsInvalidAndNonNetworkVariants() {
        assertThrows(IOException.class, () -> HlsPlaylist.variant(URI.create("https://radio.example/"), "<html>"));
        assertThrows(IOException.class, () -> HlsPlaylist.variant(URI.create("https://radio.example/"), "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1\nfile:///audio"));
        assertThrows(IOException.class, () -> HlsPlaylist.variant(URI.create("https://radio.example/"), "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1"));
    }
}
