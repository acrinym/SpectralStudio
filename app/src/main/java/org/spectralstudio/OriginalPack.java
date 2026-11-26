package org.spectralstudio;
import android.media.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.zip.*;
/** Local user-supplied verified MIX archive. One-shot playback, not original loop semantics. */
final class OriginalPack implements AutoCloseable {
    private static final long SIZE=99772192L;
    private static final String HASH="d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30";
    private static final String ROOT="assets/defaultSoundPack/";
    private final File directory;
    private final String[] names=new String[12];
    private final MediaPlayer[] players=new MediaPlayer[12];
    private final boolean[] ready=new boolean[12];
    private boolean closed;
    private static String hex(byte[] bytes){StringBuilder b=new StringBuilder();for(byte v:bytes)b.append(String.format(Locale.ROOT,"%02x",v&255));return b.toString();}
    static void importArchive(InputStream input,File root)throws Exception{
        File dst=new File(root,"original-mix");
        File tmp=File.createTempFile("mix-import-",".apk",root);
        File stage=new File(root,"mix-stage-"+UUID.randomUUID());
        try{
            MessageDigest sha=MessageDigest.getInstance("SHA-256");long size=0;
            try(OutputStream out=new FileOutputStream(tmp)){byte[] buf=new byte[65536];int n;while((n=input.read(buf))!=-1){size+=n;if(size>SIZE)throw new IOException("Not the preserved MIX 1.2.2 APK");sha.update(buf,0,n);out.write(buf,0,n);}}
            if(size!=SIZE||!HASH.equals(hex(sha.digest())))throw new IOException("Original APK fingerprint mismatch");
            if(dst.exists())throw new IOException("Original pack already imported");
            if(!stage.mkdir())throw new IOException("Cannot create import staging area");
            JSONArray labels=new JSONArray();
            try(ZipFile zip=new ZipFile(tmp)){
                JSONObject meta;
                try(InputStream in=zip.getInputStream(zip.getEntry(ROOT+"info.json"));ByteArrayOutputStream out=new ByteArrayOutputStream()){copy(in,out);meta=new JSONObject(out.toString("UTF-8"));}
                JSONObject presets=meta.getJSONObject("presets"),sounds=meta.getJSONObject("sounds");
                if(presets.length()!=1||sounds.length()!=12)throw new IOException("Unexpected preset");
                JSONArray ids=presets.getJSONObject(presets.keys().next()).getJSONArray("soundIDs");
                if(ids.length()!=12)throw new IOException("Unexpected sample count");
                Set<String> seen=new HashSet<>();
                for(int i=0;i<12;i++){
                    JSONObject s=sounds.getJSONObject(ids.getString(i));String p=s.getString("soundPath");String n=p.substring(p.lastIndexOf('/')+1);
                    if(!n.matches("[0-9a-f-]{36}\\.m4a")||!seen.add(n))throw new IOException("Invalid sample name");
                    ZipEntry e=zip.getEntry(ROOT+"Sounds/"+n);
                    if(e==null||e.getSize()<=0||e.getSize()>2000000)throw new IOException("Invalid sample entry");
                    try(InputStream in=zip.getInputStream(e);OutputStream out=new FileOutputStream(new File(stage,i+".m4a"))){copy(in,out);}
                    labels.put(s.getString("name"));
                }
                try(InputStream in=zip.getInputStream(zip.getEntry(ROOT+"info.json"));OutputStream out=new FileOutputStream(new File(stage,"info.json"))){copy(in,out);}
            }
            try(OutputStream out=new FileOutputStream(new File(stage,"labels.json"))){out.write(labels.toString().getBytes(StandardCharsets.UTF_8));}
            if(dst.exists()||!stage.renameTo(dst))throw new IOException("Cannot finalize pack import");
        }finally{tmp.delete();File[] children=stage.listFiles();if(children!=null)for(File f:children)f.delete();stage.delete();}
    }
    private static void copy(InputStream in,OutputStream out)throws IOException{byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
    OriginalPack(File directory)throws Exception{
        this.directory=directory;JSONArray labels;
        try(InputStream in=new FileInputStream(new File(directory,"labels.json"));ByteArrayOutputStream out=new ByteArrayOutputStream()){copy(in,out);labels=new JSONArray(out.toString("UTF-8"));}
        if(labels.length()!=12)throw new IOException("Invalid saved pack");for(int i=0;i<12;i++){names[i]=labels.getString(i);if(!new File(directory,i+".m4a").isFile())throw new IOException("Missing sample "+i);}
    }
    String name(int i){return names[i];}
    int readyCount(){int n=0;for(boolean b:ready)if(b)n++;return n;}
    void prepare(Runnable changed){
        for(int i=0;i<12;i++){final int index=i;MediaPlayer p=new MediaPlayer();players[i]=p;
            try{p.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                p.setDataSource(new File(directory,i+".m4a").getAbsolutePath());
                p.setOnPreparedListener(m->{if(!closed){ready[index]=true;changed.run();}});
                p.setOnErrorListener((m,w,e)->{ready[index]=false;if(!closed)changed.run();return true;});p.prepareAsync();}
            catch(IOException|RuntimeException e){ready[i]=false;p.release();players[i]=null;changed.run();}
        }
    }
    boolean play(int i,int velocity){if(closed||!ready[i])return false;try{MediaPlayer p=players[i];float v=velocity/127f;p.setVolume(v,v);p.seekTo(0);p.start();return true;}catch(IllegalStateException e){ready[i]=false;return false;}}
    void stop(){for(int i=0;i<12;i++)if(ready[i])try{players[i].pause();players[i].seekTo(0);}catch(IllegalStateException ignored){}}
    @Override public void close(){closed=true;for(int i=0;i<12;i++){ready[i]=false;if(players[i]!=null){players[i].release();players[i]=null;}}}
}
