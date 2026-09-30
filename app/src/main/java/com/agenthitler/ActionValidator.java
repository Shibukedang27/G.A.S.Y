package com.agenthitler;
import java.util.*;
public final class ActionValidator { public static boolean valid(CommandPlan p){ if(p==null||p.actions.isEmpty()||p.actions.size()>20)return false; for(Action a:p.actions){if(a==null||a.type==null)return false; if((a.type==Action.Type.OPEN_APP&&!a.args.containsKey("package"))||(a.type==Action.Type.OPEN_URL&&!a.args.containsKey("url"))||(a.type==Action.Type.SEARCH_WEB&&!a.args.containsKey("query"))||(a.type==Action.Type.SCROLL&&!a.args.containsKey("direction")))return false;} return true; } }
