package com.agenthitler;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
public class AgentAccessibilityService extends AccessibilityService {
    public static AgentAccessibilityService instance;
    @Override public void onServiceConnected(){instance=this;}
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    public ActionResult execute(Action a){return new ActionExecutor(this).execute(a);}
}
