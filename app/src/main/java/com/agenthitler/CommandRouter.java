package com.agenthitler;
import java.util.*; import java.util.regex.*;
public final class CommandRouter {
  public static CommandPlan parse(String raw){ String s=raw==null?"":raw.trim().toLowerCase(Locale.US); if(s.isEmpty()) return null; List<Action>a=new ArrayList<>();
    if(s.matches("open youtube|launch youtube")){a.add(openApp("com.google.android.youtube"));}
    else if(s.matches("open chrome|launch chrome")){a.add(openApp("com.android.chrome"));}
    else if(s.matches("go home|home")) a.add(new Action(Action.Type.HOME,null));
    else if(s.matches("go back|back")) a.add(new Action(Action.Type.BACK,null));
    else if(s.matches("take a screenshot|screenshot")) a.add(new Action(Action.Type.TAKE_SCREENSHOT,null));
    else if(s.matches("turn volume up|volume up")) a.add(new Action(Action.Type.VOLUME_UP,null));
    else if(s.matches("turn volume down|volume down")) a.add(new Action(Action.Type.VOLUME_DOWN,null));
    else if(s.matches("scroll down|scroll up")){Map<String,Object>m=new HashMap<>();m.put("direction",s.endsWith("up")?"up":"down");a.add(new Action(Action.Type.SCROLL,m));}
    else {Matcher m=Pattern.compile("search (youtube|google|web) for (.+)").matcher(s); if(m.matches()){Map<String,Object>x=new HashMap<>();x.put("query",m.group(2));x.put("provider",m.group(1));a.add(new Action(Action.Type.SEARCH_WEB,x));} else return null;}
    return new CommandPlan(a,"Certainly, sir."); }
  private static Action openApp(String pkg){Map<String,Object>m=new HashMap<>();m.put("package",pkg);return new Action(Action.Type.OPEN_APP,m);}
}
