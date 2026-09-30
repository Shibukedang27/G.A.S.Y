package com.agenthitler;
import android.content.Context;
public final class VoiceProfileStore {
    private final android.content.SharedPreferences prefs;
    public VoiceProfileStore(Context c){prefs=c.getSharedPreferences("voice_profile",Context.MODE_PRIVATE);}
    public void save(float[] f){StringBuilder s=new StringBuilder();for(float v:f){if(s.length()>0)s.append(',');s.append(v);}prefs.edit().putString("fingerprint",s.toString()).apply();}
    public float[] load(){String s=prefs.getString("fingerprint","");if(s.isEmpty())return null;String[] p=s.split(",");float[] f=new float[p.length];try{for(int i=0;i<p.length;i++)f[i]=Float.parseFloat(p[i]);return f;}catch(Exception e){return null;}}
    public boolean enrolled(){return prefs.contains("fingerprint");}
}
