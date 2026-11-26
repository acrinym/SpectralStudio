package org.spectralstudio;

import java.io.*;
import java.nio.*;

/** Original procedural, decaying instrument samples; no bundled third-party audio. */
public final class NoteSamples {
    public static final int RATE = 44100;
    private NoteSamples() {}
    public static double frequency(int note) { return 440.0 * Math.pow(2, (note - 69) / 12.0); }
    public static byte[] wave(int note) {
        if (note < 0 || note > 127) throw new IllegalArgumentException("MIDI note");
        int frames = RATE / 4, bytes = frames * 2;
        ByteBuffer b = ByteBuffer.allocate(44 + bytes).order(ByteOrder.LITTLE_ENDIAN);
        b.put(new byte[]{'R','I','F','F'}).putInt(36+bytes).put(new byte[]{'W','A','V','E','f','m','t',' '});
        b.putInt(16).putShort((short)1).putShort((short)1).putInt(RATE).putInt(RATE*2).putShort((short)2).putShort((short)16);
        b.put(new byte[]{'d','a','t','a'}).putInt(bytes);
        double f = frequency(note);
        for (int i=0; i<frames; i++) {
            double t=(double)i/RATE;
            double env=Math.min(1,t/0.005)*Math.exp(-12*t)*Math.min(1,(frames-i)/(RATE*0.015));
            double s=Math.sin(2*Math.PI*f*t);
            if (2*f<RATE/2.0) s+=0.22*Math.sin(4*Math.PI*f*t);
            b.putShort((short)(22000*env*s/1.22));
        }
        return b.array();
    }
}
