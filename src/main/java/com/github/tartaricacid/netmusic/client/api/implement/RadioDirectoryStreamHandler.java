package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.radio.RadioDirectoryApi;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class RadioDirectoryStreamHandler implements IAudioStreamHandler {
    @Override public boolean canHandle(URL url) { return RadioDirectoryApi.isStationUrl(url.toString()); }
    @Override public AudioInputStream handle(URL url) throws IOException, UnsupportedAudioFileException {
        URL stream = RadioDirectoryApi.resolve(url.toString()).toURL();
        return com.github.tartaricacid.netmusic.util.BigMegaphoneUtil.isM3u8Url(stream) ? new M3u8Handler().handle(stream) : new DirectHttpHandler().handle(stream);
    }
    @Override public int getPriority() { return 300; }
}
