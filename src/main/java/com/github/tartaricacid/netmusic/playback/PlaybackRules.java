package com.github.tartaricacid.netmusic.playback;

/** Shared hard limits; volume never changes the radius. */
public final class PlaybackRules {
    public static final int RANGE = 1024;
    private PlaybackRules() {}
    public static float gain(double squaredDistance, int volume) {
        if (!Double.isFinite(squaredDistance) || squaredDistance < 0 || squaredDistance >= (double) RANGE * RANGE) return 0;
        return (float) (Math.max(0, Math.min(100, volume)) / 100.0 * (1 - Math.sqrt(squaredDistance) / RANGE));
    }
}
