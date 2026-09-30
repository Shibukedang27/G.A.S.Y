package com.agenthitler;
import android.media.AudioRecord;
public final class VoiceMatcher {
    private final VoiceProfileStore store;
    public VoiceMatcher(android.content.Context c){store=new VoiceProfileStore(c);}
    public boolean enrolled(){return store.enrolled();}
    public void enroll(AudioRecord r,long ms){store.save(capture(r,ms));}
    public boolean matches(AudioRecord r,long ms){float[] a=store.load();return a==null||distance(a,capture(r,ms))<0.35f;}
    private static float[] capture(AudioRecord r,long ms){short[] b=new short[1600];float sum=0,zc=0;short prev=0;int n=0;long end=System.currentTimeMillis()+ms;while(System.currentTimeMillis()<end){int k=r.read(b,0,b.length);for(int i=0;i<k;i++){sum+=Math.abs(b[i])/32768f;if((b[i]>=0)!=(prev>=0))zc++;prev=b[i];n++;}}return new float[]{n==0?0:sum/n,n==0?0:zc/n};}
    private static float distance(float[]a,float[]b){float d=0;for(int i=0;i<Math.min(a.length,b.length);i++)d+=Math.abs(a[i]-b[i])*(i==0?.5f:4f);return d;}
}
