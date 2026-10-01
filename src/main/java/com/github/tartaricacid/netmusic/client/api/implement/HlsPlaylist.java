package com.github.tartaricacid.netmusic.client.api.implement;

import java.io.IOException;
import java.net.URI;

public final class HlsPlaylist {
    private HlsPlaylist() {}
    public static URI variant(URI base, String playlist) throws IOException {
        if (!playlist.stripLeading().startsWith("#EXTM3U")) throw new IOException("Invalid HLS playlist");
        boolean next = false;
        for (String raw : playlist.split("\\R")) {
            String line = raw.strip();
            if (line.startsWith("#EXT-X-STREAM-INF:")) { next = true; continue; }
            if (next && !line.isEmpty() && !line.startsWith("#")) {
                URI uri = base.resolve(line);
                if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                    throw new IOException("Unsupported HLS variant protocol");
                return uri;
            }
        }
        if (next) throw new IOException("HLS variant URL missing");
        return null;
    }
}
