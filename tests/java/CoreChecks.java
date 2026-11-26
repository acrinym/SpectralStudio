import org.spectralstudio.*;
import java.util.*;
import java.nio.*;
public class CoreChecks {
    static void check(boolean value){if(!value)throw new AssertionError();}
    static byte[] b(int... values){byte[] r=new byte[values.length];for(int i=0;i<r.length;i++)r[i]=(byte)values[i];return r;}
    static void send(MidiNotes m,int... values){byte[] r=b(values);m.accept(r,0,r.length);}
    public static void main(String[] args){
        List<String> seen=new ArrayList<>();
        MidiNotes m=new MidiNotes((c,n,v,on)->seen.add(c+":"+n+":"+v+":"+on));
        send(m,0x92,60);send(m,0xf8,100,61,0,0x82,62,40);
        check(seen.equals(List.of("2:60:100:true","2:61:0:false","2:62:40:false")));
        send(m,0xc0,1,2,0xb0,7,100,0xf0,1,2,0xf7,60,90,0xf2,1,2,61,100);
        check(seen.size()==3);
        send(m,0x90,64,127);check(seen.get(3).equals("0:64:127:true"));
        m.reset();send(m,65,127);check(seen.size()==4);
        m.accept(b(0,0x90,66,99,0),1,3);check(seen.get(4).equals("0:66:99:true"));
        check(NoteSamples.frequency(69)==440.0);
        byte[] wave=NoteSamples.wave(60);ByteBuffer w=ByteBuffer.wrap(wave).order(ByteOrder.LITTLE_ENDIAN);
        check(w.getInt(4)==wave.length-8);check(w.getInt(40)==wave.length-44);check(w.getInt(24)==44100);
        boolean audible=false;for(int i=44;i<wave.length;i+=2)if(w.getShort(i)!=0)audible=true;check(audible);
        PadMapping map=new PadMapping();
        check(map.find(0,60)==-1);map.assign(0,0,60);check(map.find(0,60)==0);
        map.assign(1,0,60);check(map.key(0)==-1&&map.find(0,60)==1);
        map.assign(0,1,60);check(map.find(1,60)==0&&map.find(0,60)==1);
        map.assign(0,2,70);check(map.find(1,60)==-1&&map.find(2,70)==0);
        map.clear();check(map.key(1)==-1&&map.key(0)==-1);
        SpecdrumsProtocol.Tap tap=SpecdrumsProtocol.decodeTap(b(255,128,0,240,4,99));
        check(tap.red()==255&&tap.green()==128&&tap.blue()==0&&tap.confidence()==240&&tap.classification()==4&&tap.intensity()==99&&tap.identified());
        check(!SpecdrumsProtocol.decodeTap(b(0,0,0,0,255,0)).identified());
        try{SpecdrumsProtocol.decodeTap(b(1,2,3,4,5));throw new AssertionError();}catch(IllegalArgumentException expected){}
        var palette=List.of(new SpecdrumsProtocol.Color(0,255,0,0),new SpecdrumsProtocol.Color(11,0,0,255));
        check(SpecdrumsProtocol.decodePalette(SpecdrumsProtocol.encodePalette(palette)).equals(palette));
        check(Arrays.equals(SpecdrumsProtocol.identify(1,2,3,255),b(1,2,3,255)));
        check(SpecdrumsProtocol.decodeMatches(b(11,255)).equals(List.of(new SpecdrumsProtocol.Match(11,255))));
        check(Arrays.equals(SpecdrumsProtocol.enableTap(true),b(1)));
        check(Arrays.equals(SpecdrumsProtocol.loadPalette(1),b(1)));
        try{SpecdrumsProtocol.encodePalette(List.of(palette.get(0),palette.get(0)));throw new AssertionError();}catch(IllegalArgumentException expected){}
        System.out.println("MIDI, WAV, mapping and native payload checks passed");
    }
}
