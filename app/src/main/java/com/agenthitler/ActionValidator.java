package com.agenthitler;
import java.util.*;
public final class ActionValidator { public static boolean valid(CommandPlan p){ if(p==null||p.actions.isEmpty()||p.actions.size()>20)return false; for(Action a:p.actions)if(a==null||a.type==null)return false; return true; } }
