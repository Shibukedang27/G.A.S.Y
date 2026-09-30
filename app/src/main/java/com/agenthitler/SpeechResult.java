package com.agenthitler;
public final class SpeechResult { public final boolean success; public final String text,error; private SpeechResult(boolean s,String t,String e){success=s;text=t;error=e;} public static SpeechResult ok(String t){return new SpeechResult(true,t,null);} public static SpeechResult fail(String e){return new SpeechResult(false,null,e);} }
