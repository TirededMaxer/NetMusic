import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

class AudioDecodeProbe {
    public static void main(String[] args) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(args[0]));
        try (var input = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
            AudioInputStream encoded;
            if ((bytes[0] & 255) == 0x47) {
                var reader = (javax.sound.sampled.spi.AudioFileReader) Class.forName(args[1]).getConstructor().newInstance();
                encoded = reader.getAudioInputStream(input);
             } else if ((bytes[0] & 255) == 255 && (bytes[1] & 0xf6) == 0xf0) {
                var reader = (javax.sound.sampled.spi.AudioFileReader) Class.forName(args[1].replace("TSAudioFileReader", "AACAudioFileReader")).getConstructor().newInstance();
                encoded = reader.getAudioInputStream(input);
            } else {
                encoded = AudioSystem.getAudioInputStream(input);
            }
            AudioFormat source = encoded.getFormat();
            AudioFormat format = new AudioFormat(source.getSampleRate(), 16, source.getChannels(), true, false);
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
