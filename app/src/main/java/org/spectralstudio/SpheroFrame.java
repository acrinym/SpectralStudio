package org.spectralstudio;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/** V2 basic flags, framing and byte-stream decoder. Extended flags are explicitly unsupported. */
public final class SpheroFrame {
    public static final int SOP=0x8d,EOP=0xd8,ESC=0xab;
    public static final int RESPONSE=1,REQUEST=2,ERROR_ONLY=4,ACTIVITY=8,TARGET=16,SOURCE=32;
    public static final int MAX_BODY=4096;
    private final int flags,did,cid,seq,tid,sid,error;
    private final byte[] payload;
    public SpheroFrame(int flags,int did,int cid,int seq,int tid,int sid,int error,byte[] payload){
        if((flags&~0x3f)!=0)throw new IllegalArgumentException("Unsupported flags");
        this.flags=flags;this.did=u8(did);this.cid=u8(cid);this.seq=u8(seq);
        this.tid=optional(flags,TARGET,tid);this.sid=optional(flags,SOURCE,sid);
        this.error=optional(flags,RESPONSE,error);
        if(payload.length>MAX_BODY-8)throw new IllegalArgumentException("Payload too large");
        this.payload=payload.clone();
    }
    private static int u8(int v){if(v<0||v>255)throw new IllegalArgumentException("uint8");return v;}
    private static int optional(int flags,int bit,int v){if((flags&bit)!=0)return u8(v);if(v!=-1)throw new IllegalArgumentException("Unexpected optional field");return -1;}
    public int flags(){return flags;} public int did(){return did;} public int cid(){return cid;}
    public int seq(){return seq;} public int tid(){return tid;} public int sid(){return sid;}
    public int error(){return error;} public boolean response(){return (flags&RESPONSE)!=0;}
    public byte[] payload(){return payload.clone();}
    public static SpheroFrame command(int did,int cid,int seq,byte[] data){return new SpheroFrame(REQUEST|ACTIVITY,did,cid,seq,-1,-1,-1,data);}
    public byte[] encode(){
        ByteArrayOutputStream raw=new ByteArrayOutputStream();
        raw.write(flags);if(tid>=0)raw.write(tid);if(sid>=0)raw.write(sid);
        raw.write(did);raw.write(cid);raw.write(seq);if(error>=0)raw.write(error);
        raw.write(payload,0,payload.length);
        int sum=0;for(byte b:raw.toByteArray())sum+=b&255;raw.write(255-(sum&255));
        ByteArrayOutputStream wire=new ByteArrayOutputStream();wire.write(SOP);
        for(byte b:raw.toByteArray()){
            int v=b&255;
            if(v==SOP||v==EOP||v==ESC){wire.write(ESC);wire.write(v&~0x88);}
            else wire.write(v);
        }
        wire.write(EOP);return wire.toByteArray();
    }
    private static SpheroFrame decodeBody(byte[] b){
        if(b.length<5)throw new IllegalArgumentException("Short frame");
        int sum=0;for(byte v:b)sum+=v&255;if((sum&255)!=255)throw new IllegalArgumentException("Bad checksum");
        int p=0,f=b[p++]&255;if((f&~0x3f)!=0)throw new IllegalArgumentException("Unsupported flags");
        int limit=b.length-1;
        int header=4+(((f&TARGET)!=0)?1:0)+(((f&SOURCE)!=0)?1:0)+(((f&RESPONSE)!=0)?1:0);
        if(limit<header)throw new IllegalArgumentException("Truncated header");
        int t=(f&TARGET)!=0?b[p++]&255:-1,s=(f&SOURCE)!=0?b[p++]&255:-1;
        int d=b[p++]&255,c=b[p++]&255,q=b[p++]&255,e=(f&RESPONSE)!=0?b[p++]&255:-1;
        return new SpheroFrame(f,d,c,q,t,s,e,Arrays.copyOfRange(b,p,limit));
    }
    /** Accepts arbitrary BLE fragments and concatenated packets, resynchronizes on SOP. */
    public static final class Decoder {
        private final ByteArrayOutputStream body=new ByteArrayOutputStream();
        private final Consumer<SpheroFrame> frames;private final Consumer<String> errors;
        private boolean active,escaped;
        public Decoder(Consumer<SpheroFrame> frames,Consumer<String> errors){this.frames=frames;this.errors=errors;}
        public void reset(){active=false;escaped=false;body.reset();}
        private void bad(String why){reset();errors.accept(why);}
        public void accept(byte[] input){
            for(byte raw:input){
                int v=raw&255;
                if(v==SOP){if(active)bad("Unexpected SOP");active=true;continue;}
                if(!active)continue;
                if(escaped){
                    if(v!=0x05&&v!=0x50&&v!=0x23){bad("Invalid escape");continue;}
                    escaped=false;v|=0x88;
                }else if(v==ESC){escaped=true;continue;}
                else if(v==EOP){
                    byte[] data=body.toByteArray();reset();SpheroFrame frame;
                    try{frame=decodeBody(data);}catch(IllegalArgumentException e){errors.accept(e.getMessage());continue;}
                    frames.accept(frame);continue;
                }
                if(body.size()>=MAX_BODY){bad("Frame exceeds limit");continue;}
                body.write(v);
            }
        }
    }
}
