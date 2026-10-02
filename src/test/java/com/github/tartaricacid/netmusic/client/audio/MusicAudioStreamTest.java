package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.sound.sampled.AudioFormat;
import static org.junit.jupiter.api.Assertions.*;

class MusicAudioStreamTest {
    @Test void opensRedirectedMp3ThroughTheActualPlayerAndOutputsMonoPcm() throws Exception {
        byte[] mp3;
        try (var input = getClass().getResourceAsStream("/audio/test-tone.mp3")) { mp3 = input.readAllBytes(); }
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/outer", e -> { e.getResponseHeaders().add("Location", "/audio.mp3"); e.sendResponseHeaders(302, -1); e.close(); });
        server.createContext("/audio.mp3", e -> { e.getResponseHeaders().add("Content-Type", "audio/mpeg"); e.sendResponseHeaders(200, mp3.length); e.getResponseBody().write(mp3); e.close(); });
        server.start();
        try {
            AudioStreamHandlerManager.init();
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/outer");
            try (var stream = new NetMusicAudioStream(uri.toURL())) {
                assertEquals(1, stream.getFormat().getChannels());
                assertEquals(AudioFormat.Encoding.PCM_SIGNED, stream.getFormat().getEncoding());
                assertEquals(16, stream.getFormat().getSampleSizeInBits());
                var bytes = stream.read(8192);
                assertNotNull(bytes);
                assertEquals(8192, bytes.remaining());
            }
        } finally { server.stop(0); }
    }
}
