package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import java.net.URI;

/** Explicit live probe; normal unit tests use an offline generated fixture. */
public final class MusicPlaybackProbe {
    public static void main(String[] args) throws Exception {
        NetMusic.NET_EASE_WEB_API = new NetEaseMusic().getApi();
        AudioStreamHandlerManager.init();
        try (var stream = new NetMusicAudioStream(URI.create(args[0]).toURL())) {
            if (stream.getFormat().getChannels() != 1) throw new AssertionError("Expected mono audio");
            var data = stream.read(32768);
            if (data == null || data.remaining() < 8192) throw new AssertionError("No decoded PCM audio");
            System.out.println("Music player pipeline passed: " + stream.getFormat() + ", " + data.remaining() + " PCM bytes");
        }
    }
}
