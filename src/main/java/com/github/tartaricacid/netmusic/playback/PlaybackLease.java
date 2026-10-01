package com.github.tartaricacid.netmusic.playback;

/** Reservations include unresolved requests so an old async result can never reclaim playback. */
public final class PlaybackLease<T> {
    private T owner;
    private long generation;
    public long claim(T next) { owner = next; return ++generation; }
    public boolean owns(T candidate, long token) { return owner == candidate && token == generation; }
    public T owner() { return owner; }
    public void release(T candidate) { if (owner == candidate) { owner = null; ++generation; } }
    public void clear() { owner = null; ++generation; }
}
