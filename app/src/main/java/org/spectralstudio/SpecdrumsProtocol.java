package org.spectralstudio;
import java.util.*;
/** Command payloads derived from the original MIX DEX and Sphero SDK. No transport writes. */
public final class SpecdrumsProtocol {
    private SpecdrumsProtocol(){}
    public static final int IO=0x1a, ENABLE_TAP=0x1d, TAP=0x1e, GET_PALETTE=0x44, SET_PALETTE=0x45, IDENTIFY=0x46, LOAD_PALETTE=0x47;
    public record Tap(int red,int green,int blue,int confidence,int classification,int intensity) {
        public boolean identified(){return classification!=255;}
    }
    public record Color(int index,int red,int green,int blue){}
    public record Match(int index,int confidence){}
    private static int u(byte b){return b&255;}
    private static byte b(int i){if(i<0||i>255)throw new IllegalArgumentException("uint8");return(byte)i;}
    public static Tap decodeTap(byte[] data){
        if(data.length!=6)throw new IllegalArgumentException("Expected six-byte tap event");
        return new Tap(u(data[0]),u(data[1]),u(data[2]),u(data[3]),u(data[4]),u(data[5]));
    }
    public static byte[] enableTap(boolean enabled){return new byte[]{(byte)(enabled?1:0)};}
    public static byte[] identify(int r,int g,int blue,int confidence){return new byte[]{b(r),b(g),b(blue),b(confidence)};}
    public static byte[] loadPalette(int index){if(index!=0&&index!=1)throw new IllegalArgumentException("Documented palettes are 0 and 1");return new byte[]{(byte)index};}
    public static List<Color> decodePalette(byte[] data){
        if(data.length%4!=0||data.length>48)throw new IllegalArgumentException("Palette tuple length");
        List<Color> colors=new ArrayList<>();for(int i=0;i<data.length;i+=4)colors.add(new Color(u(data[i]),u(data[i+1]),u(data[i+2]),u(data[i+3])));
        return List.copyOf(colors);
    }
    public static byte[] encodePalette(List<Color> colors){
        if(colors.size()>12)throw new IllegalArgumentException("At most twelve palette entries");
        Set<Integer> indices=new HashSet<>();byte[] data=new byte[4*colors.size()];int i=0;
        for(Color c:colors){if(!indices.add(c.index()))throw new IllegalArgumentException("Duplicate palette slot");data[i++]=b(c.index());data[i++]=b(c.red());data[i++]=b(c.green());data[i++]=b(c.blue());}
        return data;
    }
    public static List<Match> decodeMatches(byte[] data){
        if(data.length%2!=0||data.length>24)throw new IllegalArgumentException("Match tuple length");
        List<Match> matches=new ArrayList<>();for(int i=0;i<data.length;i+=2)matches.add(new Match(u(data[i]),u(data[i+1])));
        return List.copyOf(matches);
    }
}
