package org.spectralstudio;
import java.util.Arrays;
/** One MIDI channel/note per pad; assigning a duplicate moves the binding. */
public final class PadMapping {
    private final int[] keys=new int[12];
    public PadMapping(){clear();}
    public void clear(){Arrays.fill(keys,-1);}
    private static int encode(int channel,int note){if(channel<0||channel>15||note<0||note>127)throw new IllegalArgumentException("MIDI key");return channel*128+note;}
    public int key(int pad){return keys[pad];}
    public int find(int channel,int note){int k=encode(channel,note);for(int i=0;i<keys.length;i++)if(keys[i]==k)return i;return -1;}
    public void assign(int pad,int channel,int note){if(pad<0||pad>=12)throw new IllegalArgumentException("pad");int k=encode(channel,note);int old=find(channel,note);if(old>=0)keys[old]=-1;keys[pad]=k;}
}
