package com.agenthitler;
public interface SpeechRecognizerEngine { interface Callback { void onResult(SpeechResult result); } void start(Callback callback); void stop(); void destroy(); }
