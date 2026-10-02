package com.github.tartaricacid.netmusic.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Audio CDN redirects may change HTTPS to HTTP; API requests retain their normal policy. */
public final class AudioHttpTransport {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER)
            .version(HttpClient.Version.HTTP_1_1).build();

    private AudioHttpTransport() {}

    public static HttpResponse<InputStream> open(HttpRequest request) throws IOException {
        return open(CLIENT, request);
    }

    static HttpResponse<InputStream> open(HttpClient client, HttpRequest request) throws IOException {
        for (int redirects = 0; redirects <= 8; redirects++) {
            HttpResponse<InputStream> response;
            try { response = client.send(request, HttpResponse.BodyHandlers.ofInputStream()); }
            catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IOException("Audio request interrupted", error);
            }
            int code = response.statusCode();
            if (code != 301 && code != 302 && code != 303 && code != 307 && code != 308) return response;
            try (InputStream ignored = response.body()) {
                if (redirects == 8) throw new IOException("Too many audio redirects");
                String location = response.headers().firstValue("Location")
                        .orElseThrow(() -> new IOException("Audio redirect has no Location"));
                try { request = redirect(request, response.uri().resolve(location)); }
                catch (IllegalArgumentException error) { throw new IOException("Invalid audio redirect", error); }
            }
        }
        throw new IOException("Too many audio redirects");
    }

    static HttpRequest redirect(HttpRequest previous, URI target) throws IOException {
        if (!("http".equalsIgnoreCase(target.getScheme()) || "https".equalsIgnoreCase(target.getScheme())) ||
                target.getHost() == null || target.getUserInfo() != null)
            throw new IOException("Audio redirect must be an HTTP(S) URL");
        var builder = HttpRequest.newBuilder(target).GET()
                .timeout(previous.timeout().orElse(Duration.ofSeconds(15)));
        boolean sameOrigin = previous.uri().getScheme().equalsIgnoreCase(target.getScheme()) &&
                previous.uri().getHost().equalsIgnoreCase(target.getHost()) && previous.uri().getPort() == target.getPort();
        previous.headers().map().forEach((name, values) -> {
            if (!sameOrigin && (name.equalsIgnoreCase("Cookie") || name.equalsIgnoreCase("Authorization"))) return;
            values.forEach(value -> builder.header(name, value));
        });
        return builder.build();
    }
}
