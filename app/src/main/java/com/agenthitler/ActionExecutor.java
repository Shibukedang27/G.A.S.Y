package com.agenthitler;
import android.accessibilityservice.AccessibilityService; import android.content.*; import android.graphics.Rect; import android.media.AudioManager; import android.net.Uri; import android.os.*; import android.view.accessibility.*; import java.util.*;
public final class ActionExecutor {
 private final AccessibilityService service; public ActionExecutor(AccessibilityService s){service=s;}
 public ActionResult execute(Action a){try{switch(a.type){
  case HOME: service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME); break;
  case BACK: service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); break;
  case RECENTS: service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS); break;
  case OPEN_APP: String p=(String)a.args.get("package"); Intent i=service.getPackageManager().getLaunchIntentForPackage(p); if(i==null)return ActionResult.fail(a,"App not installed: "+p); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);service.startActivity(i);break;
  case OPEN_URL: Intent u=new Intent(Intent.ACTION_VIEW, Uri.parse((String)a.args.get("url")));u.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);service.startActivity(u);break;
  case OPEN_SETTINGS: Intent settings=new Intent(android.provider.Settings.ACTION_SETTINGS);settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);service.startActivity(settings);break;
  case SEARCH_WEB: Intent q=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode((String)a.args.get("query"))));q.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);service.startActivity(q);break;
  case VOLUME_UP: volume(AudioManager.ADJUST_RAISE);break; case VOLUME_DOWN: volume(AudioManager.ADJUST_LOWER);break; case VOLUME_MUTE: volume(AudioManager.ADJUST_MUTE);break;
  case PLAY_MEDIA: media(android.view.KeyEvent.KEYCODE_MEDIA_PLAY);break; case PAUSE_MEDIA: media(android.view.KeyEvent.KEYCODE_MEDIA_PAUSE);break; case NEXT_MEDIA: media(android.view.KeyEvent.KEYCODE_MEDIA_NEXT);break; case PREVIOUS_MEDIA: media(android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS);break;
  case SCROLL: AccessibilityNodeInfo root=service.getRootInActiveWindow(); if(root==null)return ActionResult.fail(a,"No active window"); boolean down="down".equals(a.args.get("direction")); if(!root.performAction(down?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))return ActionResult.fail(a,"No scrollable node");break;
  case CLICK_TEXT: if(!clickText((String)a.args.get("text")))return ActionResult.fail(a,"Text not found");break;
  case CLICK_DESCRIPTION: if(!clickDescription((String)a.args.get("description")))return ActionResult.fail(a,"Description not found");break;
  case INPUT_TEXT: AccessibilityNodeInfo n=service.getRootInActiveWindow(); if(n==null)return ActionResult.fail(a,"No active window"); Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,(String)a.args.get("text"));if(!n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b))return ActionResult.fail(a,"Text input failed");break;
  case WAIT: long ms=Math.min(10000,Math.max(0,((Number)a.args.get("milliseconds")).longValue()));SystemClock.sleep(ms);break;
  default:return ActionResult.fail(a,"Action not implemented in this V1"); }
  return ActionResult.ok(a,"Completed"); }catch(Exception e){return ActionResult.fail(a,e.getClass().getSimpleName()+": "+e.getMessage());}}
 private void volume(int d){((AudioManager)service.getSystemService(Context.AUDIO_SERVICE)).adjustVolume(d,AudioManager.FLAG_SHOW_UI);}
 private void media(int key){Intent i=new Intent(Intent.ACTION_MEDIA_BUTTON);i.putExtra(Intent.EXTRA_KEY_EVENT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,key));service.sendBroadcast(i);}
 private boolean clickText(String s){return click(service.getRootInActiveWindow(),s,false);} private boolean clickDescription(String s){return click(service.getRootInActiveWindow(),s,true);}
 private boolean click(AccessibilityNodeInfo n,String s,boolean desc){if(n==null)return false;for(AccessibilityNodeInfo x:n.findAccessibilityNodeInfosByText(s)){if(x.isClickable())return x.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(x.getParent()!=null)return x.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);}return false;}
}
