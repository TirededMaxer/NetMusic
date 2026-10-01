package com.github.tartaricacid.netmusic.client.audio;

import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.SampleBuffer;
import javax.sound.sampled.*;
import java.io.*;

/** Decode live ADTS frames directly: the bundled file reader rejects valid streaming headers. */
public final class AdtsAudioStream {
    private AdtsAudioStream() {}
    public static AudioInputStream open(InputStream input) throws IOException {
        Frames frames = new Frames(input);
        AudioFormat format = new AudioFormat(frames.samples.getSampleRate(),
                frames.samples.getBitsPerSample(), frames.samples.getChannels(), true, false);
        return new AudioInputStream(frames, format, AudioSystem.NOT_SPECIFIED);
    }
    private static final class Frames extends InputStream {
        private final InputStream input;
        private final ADTSDemultiplexer demux;
        private final Decoder decoder;
        private final SampleBuffer samples;
        private byte[] data;
        private int offset;
        private boolean closed, ended;
        private Frames(InputStream input) throws IOException {
            this.input = input;
            try {
                demux = new ADTSDemultiplexer(input);
                decoder = Decoder.create(demux.getDecoderInfo());
                samples = new SampleBuffer(decoder.getAudioFormat());
                samples.setBigEndian(false);
                decode();
            } catch (IOException | RuntimeException error) { input.close(); throw error; }
        }
        private void decode() throws IOException {
            for (int attempts = 0; attempts < 16; attempts++) {
                decoder.decodeFrame(demux.readNextFrame(), samples);
                data = samples.getData(); offset = 0;
                if (data.length > 0) return;
            }
            throw new IOException("AAC stream produced no PCM frames");
        }
        private boolean availableFrame() throws IOException {
            if (closed) throw new IOException("Audio stream closed");
            if (ended) return false;
            if (offset >= data.length) {
                try { decode(); }
                catch (EOFException eof) { ended = true; return false; }
            }
            return true;
        }
        @Override public int read() throws IOException {
            return availableFrame() ? data[offset++] & 255 : -1;
        }
        @Override public int read(byte[] buffer, int start, int length) throws IOException {
            java.util.Objects.checkFromIndexSize(start, length, buffer.length);
            if (length == 0) return 0;
            if (!availableFrame()) return -1;
            int count = Math.min(length, data.length - offset);
            System.arraycopy(data, offset, buffer, start, count);
            offset += count; return count;
        }
        @Override public void close() throws IOException {
            if (!closed) { closed = true; input.close(); }
        }
    }
}
