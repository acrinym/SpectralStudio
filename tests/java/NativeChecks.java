import org.spectralstudio.*;
import java.util.*;
public class NativeChecks {
    static int checks;
    static void check(boolean condition){checks++;if(!condition)throw new AssertionError("Check "+checks);}
    static byte[] b(int... data){byte[] r=new byte[data.length];for(int i=0;i<data.length;i++)r[i]=(byte)data[i];return r;}
    public static void main(String[] args){
        byte[] wire=SpheroFrame.command(0x1a,0x1d,0,b(1)).encode();
        check(Arrays.equals(wire,b(0x8d,0x0a,0x1a,0x1d,0,1,0xbd,0xd8)));
        List<SpheroFrame> got=new ArrayList<>();List<String> errors=new ArrayList<>();
        SpheroFrame.Decoder decoder=new SpheroFrame.Decoder(got::add,errors::add);
        SpheroFrame addressed=new SpheroFrame(0x31,0x1a,0x44,0xab,0x12,0x01,0,b(0x8d,0xd8,0xab,0xff));
        byte[] encoded=addressed.encode();
        for(int split=0;split<=encoded.length;split++){
            got.clear();decoder.reset();
            decoder.accept(Arrays.copyOfRange(encoded,0,split));decoder.accept(Arrays.copyOfRange(encoded,split,encoded.length));
            check(got.size()==1);var p=got.get(0);check(p.seq()==0xab&&p.tid()==0x12&&p.sid()==1&&p.error()==0&&Arrays.equals(p.payload(),addressed.payload()));
        }
        check(errors.isEmpty());got.clear();
        decoder.accept(b(0x8d,0x0a,0x1a,0x1d,0,1,0xbc,0xd8));
        decoder.accept(b(0x8d,0xab,0x00,0xd8));decoder.accept(b(0x8d,0x01,0xd8));
        check(got.isEmpty()&&errors.size()==3);
        decoder.accept(b(0x8d));decoder.accept(new byte[4100]);decoder.accept(wire);check(got.size()==1&&errors.size()==4);
        decoder.accept(wire);decoder.accept(wire);check(got.size()==3);
        // Escaped checksum: 0xff - 0x72 == 0x8d.
        byte[] escapedCheck=new SpheroFrame(0,0x72,0,0,-1,-1,-1,b()).encode();
        check(Arrays.equals(escapedCheck,b(0x8d,0,0x72,0,0,0xab,5,0xd8)));decoder.accept(escapedCheck);check(got.size()==4);
        List<SpecdrumsProtocol.Tap> taps=new ArrayList<>();List<String> status=new ArrayList<>();
        NativeTapSession session=new NativeTapSession(taps::add,status::add);
        check(Arrays.equals(session.start(0),wire));
        var tap=new SpheroFrame(0,0x1a,0x1e,0xff,-1,-1,-1,b(255,0,10,200,4,60));
        session.receive(tap);check(taps.isEmpty());
        session.receive(new SpheroFrame(1,0x1a,0x1d,1,-1,-1,0,b()));check(session.state()==NativeTapSession.State.WAITING);
        session.receive(new SpheroFrame(1,0x1a,0x1d,0,-1,-1,0,b()));check(session.state()==NativeTapSession.State.ACTIVE);
        session.receive(tap);check(taps.size()==1&&taps.get(0).classification()==4);
        session.receive(new SpheroFrame(0,0x1a,0x1e,0,-1,-1,-1,b(1)));check(taps.size()==1);
        NativeTapSession failed=new NativeTapSession(taps::add,status::add);failed.start(0);failed.receive(new SpheroFrame(1,0x1a,0x1d,0,-1,-1,7,b()));check(failed.state()==NativeTapSession.State.FAILED);
        System.out.println("Native transport: "+checks+" checks passed");
    }
}
