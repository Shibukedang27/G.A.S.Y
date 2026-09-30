package com.agenthitler;
import android.content.*; import android.database.sqlite.*; import android.database.*;
public final class AssistantMemory extends SQLiteOpenHelper {
 public AssistantMemory(Context c){super(c,"agent_memory.db",null,1);}
 public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY AUTOINCREMENT, command TEXT NOT NULL, success INTEGER NOT NULL, message TEXT, created INTEGER NOT NULL)");d.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY,value TEXT NOT NULL)");}
 public void onUpgrade(SQLiteDatabase d,int o,int n){d.execSQL("DROP TABLE IF EXISTS history");d.execSQL("DROP TABLE IF EXISTS settings");onCreate(d);}
 public void record(String command, boolean success,String message){ContentValues v=new ContentValues();v.put("command",command);v.put("success",success?1:0);v.put("message",message);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("history",null,v);}
 public void set(String key,String value){ContentValues v=new ContentValues();v.put("key",key);v.put("value",value);getWritableDatabase().insertWithOnConflict("settings",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
 public String get(String key,String fallback){Cursor c=getReadableDatabase().query("settings",new String[]{"value"},"key=?",new String[]{key},null,null,null);try{return c.moveToFirst()?c.getString(0):fallback;}finally{c.close();}}
}
