package com.github.tartaricacid.netmusic.playback;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PlaybackRulesTest {
    @Test void fixedRadiusIndependentOfVolume() {
        assertEquals(1f, PlaybackRules.gain(0, 100));
        assertEquals(.5f, PlaybackRules.gain(512 * 512, 100));
        assertEquals(.25f, PlaybackRules.gain(512 * 512, 50));
        assertTrue(PlaybackRules.gain(1023 * 1023, 1) > 0);
        assertEquals(0f, PlaybackRules.gain(1024 * 1024, 100));
        assertEquals(0f, PlaybackRules.gain(1025 * 1025, 100));
        assertEquals(0f, PlaybackRules.gain(0, 0));
    }
    @Test void invalidDistancesCannotProduceNaNOrGain() {
        assertEquals(0f, PlaybackRules.gain(Double.NaN, 100));
        assertEquals(0f, PlaybackRules.gain(-1, 100));
        assertEquals(0f, PlaybackRules.gain(Double.POSITIVE_INFINITY, 100));
        assertEquals(1f, PlaybackRules.gain(0, 500));
    }
    @Test void staleAsyncReservationsCannotStealTheWorld() {
        PlaybackLease<Object> lease = new PlaybackLease<>();
        Object first = new Object(), second = new Object();
        long a = lease.claim(first), b = lease.claim(second);
        assertFalse(lease.owns(first, a));
        assertTrue(lease.owns(second, b));
        lease.release(first);
        assertTrue(lease.owns(second, b));
        lease.clear();
        assertFalse(lease.owns(second, b));
    }
    @Test void restartAtSameBlockInvalidatesEarlierRequest() {
        PlaybackLease<Object> lease = new PlaybackLease<>();
        Object block = new Object();
        long old = lease.claim(block), current = lease.claim(block);
        assertFalse(lease.owns(block, old));
        assertTrue(lease.owns(block, current));
        lease.release(block);
        assertFalse(lease.owns(block, current));
    }
}
