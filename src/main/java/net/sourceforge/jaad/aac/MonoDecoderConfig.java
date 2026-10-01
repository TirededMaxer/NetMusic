package net.sourceforge.jaad.aac;

/** Fixed mono playback does not need the fragile HE-AAC parametric stereo extension.
 * Keep SBR frequency reconstruction; SBR1 duplicates its mono core before the final downmix.
 * This class shares JAAD's package to access its package-private configuration constructor.
 */
public final class MonoDecoderConfig extends DecoderConfig {
    public MonoDecoderConfig(AudioDecoderInfo info) { super(); setAudioDecoderInfo(info); }
    @Override public boolean isPSEnabled() { return false; }
}
