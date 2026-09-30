package com.agenthitler;
public final class NativeLlm { static { System.loadLibrary("agent_llm"); } public native boolean nativeLoadModel(String path); public native void nativeUnloadModel(); public native boolean nativeIsLoaded(); public native String nativeGenerate(String prompt,int maxTokens); }
