package com.github.tartaricacid.netmusic.util;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
public final class BigMegaphoneUtil {
    private BigMegaphoneUtil() {}
    public static boolean isValidStreamUrl(String url) {
        try {
            if (url == null || url.isBlank()) return false;
            URI uri = URI.create(url.trim());
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) && uri.getHost() != null;
        } catch (Exception e) { return false; }
    }
    public static boolean isM3u8Url(URL url) {
        return url != null && isValidStreamUrl(url.toString()) && url.getPath().toLowerCase(Locale.ROOT).endsWith(".m3u8");
    }
}
