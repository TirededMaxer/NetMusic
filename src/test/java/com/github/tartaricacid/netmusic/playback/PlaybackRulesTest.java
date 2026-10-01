package com.github.tartaricacid.netmusic.playback;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PlaybackRulesTest {
    @Test void attenuationIsLinearAcrossTheEntire1024BlockRadius() {
        for (int distance = 0; distance <= 1024; distance += 64)
            assertEquals(1 - distance / 1024f, PlaybackRules.gain((double) distance * distance), 0.00001f);
        assertTrue(PlaybackRules.gain(1023 * 1023) > 0);
        assertEquals(0, PlaybackRules.gain(1025 * 1025));
    }
    @Test void overlappingRelaysNeverSumVolumes() {
        assertEquals(.5f, PlaybackRules.strongest(PlaybackRules.gain(512 * 512), PlaybackRules.gain(512 * 512)));
        assertEquals(.75f, PlaybackRules.strongest(PlaybackRules.gain(512 * 512), PlaybackRules.gain(256 * 256)));
        assertEquals(1f, PlaybackRules.strongest(PlaybackRules.strongest(1, 1), 1));
        assertEquals(0f, PlaybackRules.strongest(0, 0));
    }
    @Test void invalidDistancesCannotProduceNaNOrGain() {
        assertEquals(0f, PlaybackRules.gain(Double.NaN));
        assertEquals(0f, PlaybackRules.gain(-1));
        assertEquals(0f, PlaybackRules.gain(Double.POSITIVE_INFINITY));
    }
    @Test void staleAsyncReservationsCannotStealTheWorld() {
        PlaybackLease<Object> lease = new PlaybackLease<>();
        Object first = new Object(), second = new Object();
        long a = lease.claim(first), b = lease.claim(second);
        assertFalse(lease.owns(first, a)); assertTrue(lease.owns(second, b));
        lease.release(first); assertTrue(lease.owns(second, b));
        lease.clear(); assertFalse(lease.owns(second, b));
    }
    @Test void restartAtSameBlockInvalidatesEarlierRequest() {
        PlaybackLease<Object> lease = new PlaybackLease<>();
        Object block = new Object();
        long old = lease.claim(block), current = lease.claim(block);
        assertFalse(lease.owns(block, old)); assertTrue(lease.owns(block, current));
        lease.release(block); assertFalse(lease.owns(block, current));
    }
}
