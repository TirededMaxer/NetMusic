package com.github.tartaricacid.netmusic.client.audio;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ChunkedAudioStreamTest {
    @Test void resumesAtByteCountWhenServerIgnoresRangeAndDoesNotReopenAfterClose() throws Exception {
        byte[] audio = {(byte)200,1,2,3,4,5,6,7};
        AtomicInteger calls = new AtomicInteger();
        List<String> ranges = new ArrayList<>();
        CountDownLatch firstByteRead = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/audio", e -> {
            ranges.add(e.getRequestHeaders().getFirst("Range"));
            e.sendResponseHeaders(200, audio.length);
            if (calls.incrementAndGet() == 1) {
                e.getResponseBody().write(audio[0]);
                e.getResponseBody().flush();
                try { firstByteRead.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
            } else e.getResponseBody().write(audio);
            e.close();
        });
        server.start();
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/audio");
        try {
            var stream = new ChunkedAudioStream(start -> HttpRequest.newBuilder(uri).header("Range", "bytes="+start+"-").build());
            try (stream) {
                assertEquals(200, stream.read());
                firstByteRead.countDown();
                assertArrayEquals(new byte[]{1,2,3,4,5,6,7}, stream.readAllBytes());
                assertEquals(-1, stream.read());
                assertEquals(-1, stream.read());
                assertEquals(List.of("bytes=0-", "bytes=1-"), ranges);
            }
            assertThrows(java.io.IOException.class, stream::read);
            assertEquals(2, calls.get());
        } finally { firstByteRead.countDown(); server.stop(0); }
    }
}
