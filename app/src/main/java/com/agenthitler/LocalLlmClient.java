package com.agenthitler;
import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;
/** Optional local planner. It talks only to a loopback Termux llama.cpp server; Android actions never leave this process. */
public final class LocalLlmClient {
 public String generatePlan(String command)throws IOException{HttpURLConnection c=(HttpURLConnection)new URL("http://127.0.0.1:8080/completion").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(500);c.setReadTimeout(20000);c.setRequestProperty("Content-Type","application/json");String prompt="Return JSON only using the approved GASY action schema for: "+command;String body="{\"prompt\":"+quote(prompt)+",\"n_predict\":256,\"temperature\":0.1}";try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}if(c.getResponseCode()!=200)throw new IOException("llama.cpp HTTP "+c.getResponseCode());try(InputStream in=c.getInputStream()){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
 private static String quote(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
}
