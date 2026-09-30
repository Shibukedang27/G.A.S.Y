package com.agenthitler;
import java.util.*;
public final class CommandPlan { public final List<Action> actions; public final String response; public CommandPlan(List<Action>a,String r){actions=Collections.unmodifiableList(a);response=r;} }
