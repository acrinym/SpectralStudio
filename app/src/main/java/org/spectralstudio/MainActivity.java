package org.spectralstudio;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;
import android.media.*;
import android.media.midi.*;
import android.os.*;
import android.graphics.Color;
import android.widget.*;
import android.view.View;
import java.io.*;
import java.util.*;

public final class MainActivity extends Activity {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Map<String,BluetoothDevice> found = new LinkedHashMap<>();
    private final Set<Integer> loaded = new HashSet<>();
    private final int[] samples = new int[11];
    private BluetoothLeScanner scanner;
    private ScanCallback callback;
    private MidiDevice device;
    private MidiOutputPort port;
    private SoundPool sound;
    private TextView status, event;
    private LinearLayout devices;
    private int generation;
    private final PadMapping mapping=new PadMapping();
    private final Button[] pads=new Button[12];
    private OriginalPack pack;
    private boolean packMode, importing;
    private int learnPad=-1;
    private Button mode, learn;
    private TextView packStatus;
    private boolean destroyed;
    private static final String MIDI_UUID="03b80e5a-ede8-4b33-a751-6ce34ec4c700";
    private final int[] colors={0xffef5350,0xffff9800,0xffffd54f,0xff8bc34a,0xff43a047,0xff26a69a,0xff29b6f6,0xff3949ab,0xff7e57c2,0xffec407a,0xffeeeeee,0xff999999};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,20,20,20);
        ScrollView scroll=new ScrollView(this); scroll.setClipToPadding(false); scroll.addView(root); setContentView(scroll);
        if (Build.VERSION.SDK_INT>=35) root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());
            v.setPadding(20+bars.left,20+bars.top,20+bars.right,20+bars.bottom); return insets;
        });
        TextView title=new TextView(this); title.setText("SpectralStudio"); title.setTextSize(28); root.addView(title);
        status=new TextView(this); status.setText("Preparing local sounds. Wake the ring using USB, then unplug."); root.addView(status);
        Button scan=new Button(this); scan.setText("Scan for Bluetooth MIDI devices"); scan.setOnClickListener(v->requestScan()); root.addView(scan);
        Button disconnect=new Button(this); disconnect.setText("Disconnect"); disconnect.setOnClickListener(v->{stopScan(); closeRing(); status.setText("Disconnected");}); root.addView(disconnect);
        devices=new LinearLayout(this); devices.setOrientation(LinearLayout.VERTICAL); root.addView(devices);
        event=new TextView(this); event.setText("Waiting for a MIDI note or a test pad"); root.addView(event);
        packStatus=new TextView(this); packStatus.setText("Original MIX pack not loaded"); root.addView(packStatus);
        Button importPack=new Button(this); importPack.setText("Import original MIX APK"); importPack.setOnClickListener(v->choosePack()); root.addView(importPack);
        mode=new Button(this); mode.setText("Instrument: generated notes"); mode.setOnClickListener(v->{if(pack!=null){packMode=!packMode;if(!packMode)pack.stop();refreshPads();}}); root.addView(mode);
        learn=new Button(this); learn.setText("Learn ring-to-pad mapping"); learn.setOnClickListener(v->{learnPad=learnPad==-1?-2:-1;learn.setText(learnPad==-1?"Learn ring-to-pad mapping":"Cancel learn");event.setText(learnPad==-1?"Learn cancelled":"Select a pad, then tap a physical color.");});root.addView(learn);
        Button reset=new Button(this);reset.setText("Reset learned mapping");reset.setOnClickListener(v->{mapping.clear();getPreferences(0).edit().clear().apply();learnPad=-1;learn.setText("Learn ring-to-pad mapping");refreshPads();});root.addView(reset);
        for(int i=0;i<12;i++) {int key=getPreferences(0).getInt("pad"+i,-1);if(key>=0&&key<2048)mapping.assign(i,key/128,key%128);}
        TextView note=new TextView(this); note.setText("Pad colors are illustrative. Learn your actual ring notes; no firmware palette changes."); root.addView(note);
        for(int i=0;i<12;i+=3) {
            LinearLayout row=new LinearLayout(this); root.addView(row);
            for(int j=i;j<i+3;j++) {
                final int index=j; Button pad=new Button(this); pads[index]=pad; pad.setText("Note "+(60+index)); pad.setTextColor(Color.BLACK); pad.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colors[j]));
                pad.setOnClickListener(v->{if(learnPad!=-1){learnPad=index;event.setText("Tap the physical color for pad "+(index+1));}else triggerPad(index,100,"Local pad");}); row.addView(pad,new LinearLayout.LayoutParams(0,100,1));
            }
        }
        prepareSounds(); refreshPads(); openSavedPack();
    }
    private void refreshPads(){
        mode.setText(packMode?"Instrument: original MIX (one-shot)":"Instrument: generated notes");
        for(int i=0;i<12;i++){int key=mapping.key(i);String label=packMode&&pack!=null?pack.name(i):"Note "+(60+i);pads[i].setText(label+(key<0?"":"\nCH "+(key/128+1)+" / "+key%128));}
    }
    private void triggerPad(int index,int velocity,String source){
        if(packMode&&pack!=null){event.setText(source+": "+pack.name(index)+(pack.play(index,velocity)?"":" (sample not ready)"));}
        else play(60+index,velocity,source);
    }
    private void ringNote(int channel,int note,int velocity){
        if(learnPad>=0){mapping.assign(learnPad,channel,note);android.content.SharedPreferences.Editor e=getPreferences(0).edit();for(int i=0;i<12;i++)e.putInt("pad"+i,mapping.key(i));e.apply();learnPad=-1;learn.setText("Learn ring-to-pad mapping");refreshPads();event.setText("Learned channel "+(channel+1)+", note "+note);return;}
        if(learnPad==-2){event.setText("Select a pad first.");return;}
        int index=mapping.find(channel,note);
        if(index>=0)triggerPad(index,velocity,"Ring");
        else if(!packMode)play(note,velocity,"Ring ch "+(channel+1));
        else event.setText("Unmapped MIDI note "+note+"; use Learn to assign it.");
    }
    private void choosePack(){
        if(importing)return;
        android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);intent.setType("*/*");
        startActivityForResult(intent,42);
    }
    @Override protected void onActivityResult(int request,int result,android.content.Intent data){
        super.onActivityResult(request,result,data);
        if(request!=42||result!=RESULT_OK||data==null||data.getData()==null||importing)return;
        importing=true;packStatus.setText("Verifying original APK and importing audio…");
        android.net.Uri uri=data.getData();
        new Thread(()->{try{
            try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Cannot open APK");OriginalPack.importArchive(in,getFilesDir());}
            ui.post(()->{if(!destroyed){importing=false;openSavedPack();}});
        }catch(Exception e){ui.post(()->{if(!destroyed){importing=false;packStatus.setText("Import failed: "+e.getMessage());}});}},"mix-import").start();
    }
    private void openSavedPack(){
        File dir=new File(getFilesDir(),"original-mix");
        if(!dir.isDirectory())return;
        try{if(pack!=null)pack.close();pack=new OriginalPack(dir);packMode=true;packStatus.setText("Original MIX Intro: 12 imported samples; preparing audio");pack.prepare(()->{if(!destroyed)packStatus.setText("Original MIX Intro: "+pack.readyCount()+"/12 samples ready");});refreshPads();}
        catch(Exception e){pack=null;packMode=false;packStatus.setText("Pack unavailable: "+e.getMessage());refreshPads();}
    }
    private void prepareSounds() {
        sound=new SoundPool.Builder().setMaxStreams(12).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).build();
        sound.setOnLoadCompleteListener((pool,id,result)->ui.post(()->{if(destroyed)return;if(result==0)loaded.add(id);else status.setText("A generated sound failed to load: "+result);}));
        try {
            for(int octave=0;octave<samples.length;octave++) {
                File f=new File(getCacheDir(),"spectral-note-"+octave+".wav");
                try(FileOutputStream out=new FileOutputStream(f)){out.write(NoteSamples.wave(12*octave));}
                samples[octave]=sound.load(f.getAbsolutePath(),1);
            }
        } catch(IOException e){status.setText("Audio preparation failed: "+e.getMessage());}
    }
    private void play(int note,int velocity,String source) {
        if(destroyed || note<0 || note>127 || velocity<=0)return;
        int octave=note/12, id=samples[octave];
        if(!loaded.contains(id)){event.setText("Audio loading; retry note "+note);return;}
        float vol=velocity/127f, rate=(float)Math.pow(2,(note%12)/12.0);
        int stream=sound.play(id,vol,vol,1,0,rate);
        event.setText(source+": note "+note+" / velocity "+velocity+(stream==0?" (audio playback failed)":""));
    }
    private void requestScan() {
        String[] perms=Build.VERSION.SDK_INT>=31?new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}:new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        for(String p:perms) if(checkSelfPermission(p)!=PackageManager.PERMISSION_GRANTED){requestPermissions(perms,1);return;}
        startScan();
    }
    @Override public void onRequestPermissionsResult(int request,String[] p,int[] g) {
        super.onRequestPermissionsResult(request,p,g);
        if(request!=1)return;
        if(g.length>0 && Arrays.stream(g).allMatch(v->v==PackageManager.PERMISSION_GRANTED))requestScan();
        else status.setText("Bluetooth permission is required for ring discovery.");
    }
    private void startScan() {
        stopScan(); closeRing(); found.clear(); devices.removeAllViews();
        BluetoothManager manager=getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter=manager==null?null:manager.getAdapter();
        if(adapter==null || !adapter.isEnabled()){status.setText("Enable Bluetooth in system settings first.");return;}
        scanner=adapter.getBluetoothLeScanner();
        if(scanner==null){status.setText("BLE scanner unavailable.");return;}
        callback=new ScanCallback(){
            @Override public void onScanResult(int type, ScanResult result){ui.post(()->{
                if(callback!=this || destroyed)return;
                ScanRecord record=result.getScanRecord();
                String name=record==null?null:record.getDeviceName();
                if(name==null)name=result.getDevice().getName();
                List<ParcelUuid> uuids=record==null?null:record.getServiceUuids();
                boolean midi=uuids!=null && uuids.stream().anyMatch(u->MIDI_UUID.equalsIgnoreCase(u.toString()));
                // Display named devices too, because legacy rings may omit the MIDI service in advertisements.
                if(!midi && (name==null || name.isEmpty()))return;
                BluetoothDevice d=result.getDevice(); String address=d.getAddress();
                if(found.putIfAbsent(address,d)!=null)return;
                Button button=new Button(MainActivity.this);
                button.setText((name==null?"Unnamed MIDI":name)+" • "+address+(midi?" [MIDI]":""));
                button.setOnClickListener(v->connect(d)); devices.addView(button);
            });}
            @Override public void onScanFailed(int code){ui.post(()->{if(callback==this){stopScan();status.setText("BLE scan failed: "+code);}});}
        };
        try {scanner.startScan(null,new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),callback);}
        catch(SecurityException e){stopScan();status.setText("Bluetooth permission unavailable.");return;}
        status.setText("Scanning for 15 seconds. Select your ring from the list.");
        final ScanCallback active=callback;
        ui.postDelayed(()->{if(callback==active){stopScan();status.setText(found.isEmpty()?"No named or MIDI devices found. Wake the ring and try again.":"Scan complete; select your ring.");}},15000);
    }
    private void stopScan() {
        ScanCallback old=callback; callback=null;
        if(scanner!=null && old!=null)try{scanner.stopScan(old);}catch(SecurityException ignored){}
        scanner=null;
    }
    private void connect(BluetoothDevice target) {
        stopScan();closeRing(); final int attempt=generation;
        MidiManager manager=getSystemService(MidiManager.class);
        if(manager==null){status.setText("Android MIDI is unavailable on this device.");return;}
        status.setText("Opening BLE MIDI connection...");
        try {manager.openBluetoothDevice(target,opened->{
            if(destroyed || attempt!=generation){close(opened);return;}
            if(opened==null){closeRing();status.setText("MIDI connection failed. Verify the ring supports BLE MIDI and is not connected elsewhere.");return;}
            device=opened;port=device.openOutputPort(0);
            if(port==null){closeRing();status.setText("Device has no available MIDI output port.");return;}
            MidiNotes parser=new MidiNotes((channel,n,velocity,on)->{
                if(on)ui.post(()->{if(attempt==generation)ringNote(channel,n,velocity);});
            });
            port.connect(new MidiReceiver(){@Override public void onSend(byte[] data,int offset,int count,long timestamp){parser.accept(data,offset,count);}});
            status.setText("MIDI connected. Tap a colored surface.");
        },ui);}catch(SecurityException e){closeRing();status.setText("Bluetooth connection permission unavailable.");}
        ui.postDelayed(()->{if(attempt==generation && device==null){closeRing();status.setText("MIDI connection timed out.");}},20000);
    }
    private static void close(Closeable c){if(c!=null)try{c.close();}catch(IOException ignored){}}
    private void closeRing(){generation++;close(port);port=null;close(device);device=null;}
    @Override protected void onStop(){stopScan();closeRing();if(pack!=null)pack.stop();status.setText("Disconnected while app is in background.");super.onStop();}
    @Override protected void onDestroy(){destroyed=true;stopScan();closeRing();ui.removeCallbacksAndMessages(null);if(sound!=null)sound.release();if(pack!=null)pack.close();super.onDestroy();}
}
