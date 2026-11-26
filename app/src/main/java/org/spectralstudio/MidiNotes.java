package org.spectralstudio;

/** MIDI 1.0 stream parser for channel note events; BLE framing is handled by Android. */
public final class MidiNotes {
    public interface Sink { void note(int channel, int note, int velocity, boolean on); }
    private final Sink sink;
    private int running, status, needed, count, first;
    private boolean sysex;
    public MidiNotes(Sink sink) { this.sink = sink; }
    public void reset() { running = status = needed = count = first = 0; sysex = false; }
    public void accept(byte[] bytes, int offset, int length) {
        if (offset < 0 || length < 0 || offset > bytes.length - length) throw new IndexOutOfBoundsException();
        for (int i = offset; i < offset + length; i++) {
            int b = bytes[i] & 255;
            if (b >= 0xf8) continue; // interleaved real-time bytes
            if (b >= 0x80) {
                if (b == 0xf0) { reset(); sysex = true; continue; }
                sysex = false; count = 0;
                if (b >= 0xf0) {
                    running = 0; status = b;
                    needed = b == 0xf2 ? 2 : (b == 0xf1 || b == 0xf3 ? 1 : 0);
                } else {
                    running = status = b;
                    int kind = b & 0xf0;
                    needed = kind == 0xc0 || kind == 0xd0 ? 1 : 2;
                }
                continue;
            }
            if (sysex || needed == 0) continue;
            if (count++ == 0) first = b;
            if (count == needed) {
                int kind = status & 0xf0;
                if (kind == 0x80 || kind == 0x90)
                    sink.note(status & 15, first, b, kind == 0x90 && b != 0);
                count = 0;
                if (running == 0) { needed = 0; status = 0; }
            }
        }
    }
}
