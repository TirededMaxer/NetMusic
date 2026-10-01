import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

class AudioDecodeProbe {
    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            var handler = Class.forName("com.github.tartaricacid.netmusic.client.api.implement.M3u8Handler").getConstructor().newInstance();
            var live = (AudioInputStream) handler.getClass().getMethod("handle", java.net.URL.class).invoke(handler, java.net.URI.create(args[2]).toURL());
            decode(live); return;
        }
        byte[] bytes = Files.readAllBytes(Path.of(args[0]));
        try (var input = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
            AudioInputStream encoded;
            if ((bytes[0] & 255) == 0x47) {
                var reader = (javax.sound.sampled.spi.AudioFileReader) Class.forName(args[1]).getConstructor().newInstance();
                encoded = reader.getAudioInputStream(input);
             } else if ((bytes[0] & 255) == 255 && (bytes[1] & 0xf6) == 0xf0) {
                encoded = (AudioInputStream) Class.forName("com.github.tartaricacid.netmusic.client.audio.AdtsAudioStream")
                        .getMethod("open", InputStream.class).invoke(null, input);
            } else {
                encoded = AudioSystem.getAudioInputStream(input);
            }
            decode(encoded);
        }
    }
    private static void decode(AudioInputStream encoded) throws Exception {
        try (encoded) {
            AudioFormat source = encoded.getFormat();
            AudioFormat format = new AudioFormat(source.getSampleRate(), 16, 1, true, false);
            try (var pcm = AudioSystem.getAudioInputStream(format, encoded)) {
                byte[] buffer = new byte[format.getFrameSize() * 2048];
                long count = 0;
                long limit = (long) (format.getFrameRate() * format.getFrameSize() * 16);
                int read;
                while (count < limit && (read = pcm.read(buffer)) != -1) count += read;
                if (count < 8192) throw new IOException("Insufficient decoded audio: " + count);
                System.out.println(source + " -> " + count + " PCM bytes");
            }
        }
    }
}
