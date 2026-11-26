package org.spectralstudio;
import java.util.function.Consumer;

/** A single enable transaction followed by tap notifications. No palette or firmware writes. */
public final class NativeTapSession {
    public enum State { IDLE, WAITING, ACTIVE, FAILED }
    private State state=State.IDLE;private int sequence;
    private final Consumer<SpecdrumsProtocol.Tap> taps;
    private final Consumer<String> status;
    public NativeTapSession(Consumer<SpecdrumsProtocol.Tap> taps,Consumer<String> status){this.taps=taps;this.status=status;}
    public State state(){return state;}
    public byte[] start(int seq){
        if(state!=State.IDLE)throw new IllegalStateException("Session already started");
        byte[] frame=SpheroFrame.command(SpecdrumsProtocol.IO,SpecdrumsProtocol.ENABLE_TAP,seq,SpecdrumsProtocol.enableTap(true)).encode();
        sequence=seq;state=State.WAITING;return frame;
    }
    public void fail(String reason){state=State.FAILED;status.accept(reason);}
    public void receive(SpheroFrame frame){
        if(frame.did()!=SpecdrumsProtocol.IO)return;
        if(state==State.WAITING&&frame.response()&&frame.cid()==SpecdrumsProtocol.ENABLE_TAP&&frame.seq()==sequence){
            if(frame.error()!=0){fail("Tap enable rejected: "+SpheroFrame.errorName(frame.error())+" ("+frame.error()+")");return;}
            state=State.ACTIVE;status.accept("Native tap enable acknowledged; waiting for color taps.");return;
        }
        if(state!=State.ACTIVE||frame.response()||frame.cid()!=SpecdrumsProtocol.TAP)return;
        try{taps.accept(SpecdrumsProtocol.decodeTap(frame.payload()));}
        catch(IllegalArgumentException e){status.accept("Malformed native tap: "+e.getMessage());}
    }
}
