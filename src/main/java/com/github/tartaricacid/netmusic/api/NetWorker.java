package com.github.tartaricacid.netmusic.api;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NetWorker {
    public static final String USER_AGENT = "Mozilla/5.0 (NetMusic)";
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL).version(HttpClient.Version.HTTP_1_1).build();
    private static HttpRequest.Builder request(String url, Map<String, String> headers) {
        var builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15));
        headers.forEach(builder::header);
        return builder;
    }
    public static String get(String url, Map<String, String> headers) throws IOException {
        return send(request(url, headers).GET().build(), HttpResponse.BodyHandlers.ofString(UTF_8)).body();
    }
    public static String post(String url, String param, Map<String, String> headers) throws IOException {
        return send(request(url, headers).POST(HttpRequest.BodyPublishers.ofString(param, UTF_8)).build(),
                HttpResponse.BodyHandlers.ofString(UTF_8)).body();
    }
    public static <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws IOException {
        try { return HTTP_CLIENT.send(request, handler); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("HTTP request interrupted", e); }
    }
}
