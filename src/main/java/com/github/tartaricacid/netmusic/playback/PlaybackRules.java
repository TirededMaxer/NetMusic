package com.github.tartaricacid.netmusic.playback;
/** Fixed 1024-block linear attenuation, shared by both players and relay selection. */
public final class PlaybackRules {
    public static final int RANGE = 1024;
    private PlaybackRules() {}
    public static float gain(double squaredDistance) {
        if (!Double.isFinite(squaredDistance) || squaredDistance < 0 || squaredDistance >= (double) RANGE * RANGE) return 0;
        return (float) (1 - Math.sqrt(squaredDistance) / RANGE);
    }
    public static float strongest(float existing, float candidate) { return Math.max(existing, candidate); }
}
