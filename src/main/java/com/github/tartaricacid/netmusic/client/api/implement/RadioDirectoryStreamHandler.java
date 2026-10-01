package com.github.tartaricacid.netmusic.client.api.implement;
import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.radio.RadioDirectoryApi;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
public class RadioDirectoryStreamHandler implements IAudioStreamHandler {
    @Override public boolean canHandle(URL url) { return RadioDirectoryApi.isStationUrl(url.toString()); }
    @Override public AudioInputStream handle(URL url) throws IOException, UnsupportedAudioFileException {
        IOException failure = new IOException("No available stream for this station");
        for (var candidate : RadioDirectoryApi.resolveCandidates(url.toString())) {
            try {
                URL stream = candidate.toURL();
                return BigMegaphoneUtil.isM3u8Url(stream) ? new M3u8Handler().handle(stream) : new DirectHttpHandler().handle(stream);
            } catch (IOException | UnsupportedAudioFileException | RuntimeException error) { failure.addSuppressed(error); }
        }
        throw failure;
    }
    @Override public int getPriority() { return 300; }
}
