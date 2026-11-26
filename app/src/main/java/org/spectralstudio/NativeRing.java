package org.spectralstudio;
import android.bluetooth.*;
import android.content.Context;
import android.os.Handler;
import java.util.UUID;
import java.util.function.Consumer;

/** Experimental direct GATT path; deliberately no undocumented unlock, wake or DFU writes. */
final class NativeRing implements AutoCloseable {
    static final UUID SERVICE=UUID.fromString("00010001-574f-4f20-5370-6865726f2121");
    static final UUID API=UUID.fromString("00010002-574f-4f20-5370-6865726f2121");
    static final UUID CCCD=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final Handler ui;
    private final Consumer<String> status,trace;
    private final NativeTapSession session;
    private final SpheroFrame.Decoder decoder;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic api;
    private boolean closed,subscribed;
    NativeRing(Handler ui,Consumer<SpecdrumsProtocol.Tap> taps,Consumer<String> status,Consumer<String> trace){
        this.ui=ui;this.status=status;this.trace=trace;
        session=new NativeTapSession(taps,s->{status.accept(s);if(sessionFailed())close();});
        decoder=new SpheroFrame.Decoder(session::receive,e->{trace.accept("Decode error: "+e);status.accept("Native packet rejected: "+e);});
    }
    private void logBytes(String direction,byte[] bytes){
        StringBuilder b=new StringBuilder(direction);
        for(byte v:bytes)b.append(String.format(java.util.Locale.ROOT," %02X",v&255));
        trace.accept(b.toString());
    }
    private boolean sessionFailed(){return session.state()==NativeTapSession.State.FAILED;}
    void connect(Context context,BluetoothDevice target){
        try{gatt=target.connectGatt(context,false,callback,BluetoothDevice.TRANSPORT_LE);}
        catch(SecurityException e){fail("Bluetooth permission unavailable");return;}
        if(gatt==null){fail("Native GATT connection could not start");return;}
        ui.postDelayed(()->{if(!closed&&session.state()!=NativeTapSession.State.ACTIVE)fail("Native handshake/tap-enable timed out; ring may require initialization");},20000);
    }
    private boolean live(BluetoothGatt g){return !closed&&gatt==g;}
    private void fail(String s){status.accept(s);close();}
    private final BluetoothGattCallback callback=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt g,int result,int state){ui.post(()->{
            if(!live(g))return;
            if(result!=BluetoothGatt.GATT_SUCCESS||state!=BluetoothProfile.STATE_CONNECTED){fail("Native GATT disconnected: "+result);return;}
            try{if(!g.discoverServices())fail("Native discovery could not start");}catch(SecurityException e){fail("Bluetooth permission lost");}
        });}
        @Override public void onServicesDiscovered(BluetoothGatt g,int result){ui.post(()->{
            if(!live(g))return;
            if(result!=BluetoothGatt.GATT_SUCCESS){fail("Native service discovery failed: "+result);return;}
            BluetoothGattService svc=g.getService(SERVICE);
            api=svc==null?null:svc.getCharacteristic(API);
            if(api==null){fail("Expected V2 API characteristic absent; use GATT inventory");return;}
            int properties=api.getProperties();
            BluetoothGattDescriptor d=api.getDescriptor(CCCD);
            if(d==null||(properties&BluetoothGattCharacteristic.PROPERTY_WRITE)==0||
                (properties&(BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_INDICATE))==0){
                fail("Expected acknowledged write and notification properties absent");return;
            }
            try{
                if(!g.setCharacteristicNotification(api,true)){fail("Notification registration failed");return;}
                d.setValue((properties&BluetoothGattCharacteristic.PROPERTY_NOTIFY)!=0?BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE:BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                if(!g.writeDescriptor(d))fail("Notification descriptor write could not start");
            }catch(SecurityException e){fail("Bluetooth permission lost");}
        });}
        @Override public void onDescriptorWrite(BluetoothGatt g,BluetoothGattDescriptor d,int result){ui.post(()->{
            if(!live(g)||api==null||!CCCD.equals(d.getUuid())||!API.equals(d.getCharacteristic().getUuid())||subscribed)return;
            if(result!=BluetoothGatt.GATT_SUCCESS){fail("Notification subscription failed: "+result);return;}
            subscribed=true;
            try{
                api.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                byte[] packet=session.start(0);logBytes("TX",packet);api.setValue(packet);
                if(!g.writeCharacteristic(api))fail("Tap enable write could not start");
                else status.accept("Native tap enable sent; awaiting protocol acknowledgement");
            }catch(SecurityException e){fail("Bluetooth permission lost");}
        });}
        @Override public void onCharacteristicWrite(BluetoothGatt g,BluetoothGattCharacteristic c,int result){ui.post(()->{
            if(live(g)&&API.equals(c.getUuid())&&result!=BluetoothGatt.GATT_SUCCESS)fail("Tap enable BLE write failed: "+result);
        });}
        @Override public void onCharacteristicChanged(BluetoothGatt g,BluetoothGattCharacteristic c){
            byte[] value=c.getValue();if(value!=null)deliver(g,c.getUuid(),value.clone());
        }
        @Override public void onCharacteristicChanged(BluetoothGatt g,BluetoothGattCharacteristic c,byte[] value){
            deliver(g,c.getUuid(),value.clone());
        }
        private void deliver(BluetoothGatt g,UUID id,byte[] data){ui.post(()->{if(live(g)&&subscribed&&API.equals(id)){logBytes("RX",data);decoder.accept(data);}});}
    };
    @Override public void close(){if(closed)return;closed=true;decoder.reset();BluetoothGatt g=gatt;gatt=null;if(g!=null){try{g.disconnect();}catch(SecurityException ignored){}try{g.close();}catch(SecurityException ignored){}}}
}
