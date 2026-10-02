package com.github.tartaricacid.netmusic.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import static org.junit.jupiter.api.Assertions.*;

class AudioHttpTransportTest {
    @Test void followsAudioDowngradeWithoutSendingAccountHeaders() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("https://music.163.com/outer"))
                .header("Range", "bytes=321-").header("Cookie", "test-only")
                .header("Authorization", "test-only").header("User-Agent", "NetMusic").build();
        var next = AudioHttpTransport.redirect(request, URI.create("http://cdn.example/song.mp3"));
        assertEquals("http", next.uri().getScheme());
        assertEquals("bytes=321-", next.headers().firstValue("Range").orElseThrow());
        assertEquals("NetMusic", next.headers().firstValue("User-Agent").orElseThrow());
        assertTrue(next.headers().firstValue("Cookie").isEmpty());
        assertTrue(next.headers().firstValue("Authorization").isEmpty());
    }

    @Test void readsRedirectedBodyAndLimitsRedirectLoops() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/start", e -> { e.getResponseHeaders().add("Location", "/audio"); e.sendResponseHeaders(302, -1); e.close(); });
        server.createContext("/audio", e -> { e.sendResponseHeaders(200, 3); e.getResponseBody().write(new byte[]{1,2,3}); e.close(); });
        server.createContext("/loop", e -> { e.getResponseHeaders().add("Location", "/loop"); e.sendResponseHeaders(302, -1); e.close(); });
        server.start();
        URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        try {
            var response = AudioHttpTransport.open(HttpRequest.newBuilder(base.resolve("/start")).build());
            assertEquals(200, response.statusCode());
            try (var body = response.body()) { assertArrayEquals(new byte[]{1,2,3}, body.readAllBytes()); }
            assertThrows(java.io.IOException.class, () -> AudioHttpTransport.open(HttpRequest.newBuilder(base.resolve("/loop")).build()));
        } finally { server.stop(0); }
    }

    @Test void rejectsNonNetworkRedirects() {
        var request = HttpRequest.newBuilder(URI.create("https://music.163.com/outer")).build();
        assertThrows(java.io.IOException.class, () -> AudioHttpTransport.redirect(request, URI.create("file:///song.mp3")));
    }
}
