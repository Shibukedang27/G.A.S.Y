package com.agenthitler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.content.pm.PackageManager;
import android.util.Log;

public class MainActivity extends Activity {
    public static final String ACTION_TEST = "com.agenthitler.TEST_COMMAND";
    public static final String ACTION_INSTALL_LLM = "com.agenthitler.INSTALL_LLM";
    public static final String ACTION_TEST_LLM = "com.agenthitler.TEST_LLM";
    public static final String ACTION_TEST_NATIVE = "com.agenthitler.TEST_NATIVE";
    private TextView status;
    private TextToSpeech tts;
    private SpeechRecognizerEngine speech;
    private AssistantMemory memory;
    private AssistantState state=AssistantState.IDLE;
    private WakePhraseListener wake;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(40,60,40,40); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setBackgroundColor(Color.rgb(10,10,10));
        TextView title = new TextView(this); title.setText("GASY"); title.setTextColor(Color.RED); title.setTextSize(28); root.addView(title);
        status = new TextView(this); status.setText("Ready. Say: GASY"); status.setTextColor(Color.WHITE); status.setTextSize(16); status.setPadding(0,30,0,30); root.addView(status);
        Button test = new Button(this); test.setText("RUN TEST COMMAND"); test.setOnClickListener(v -> handleTest()); root.addView(test);
        Button listen = new Button(this); listen.setText("LISTEN FOR COMMAND"); listen.setOnClickListener(v -> listen()); root.addView(listen);
        Button wakeButton = new Button(this); wakeButton.setText("START GASY WAKE LISTENER"); wakeButton.setOnClickListener(v -> startWakeListener()); root.addView(wakeButton);
        Button llm = new Button(this); llm.setText("INSTALL LOCAL LLM IN TERMUX"); llm.setOnClickListener(v -> installLocalLlm()); root.addView(llm);
        Button settings = new Button(this); settings.setText("OPEN ACCESSIBILITY SETTINGS"); settings.setOnClickListener(v -> startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"))); root.addView(settings);
        setContentView(root);
        tts = new TextToSpeech(this, s -> { if (s == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US); });
        speech = new VoskOfflineSpeechRecognizer(this);
        memory = new AssistantMemory(this);
        wake = new WakePhraseListener(speech);
        if(ACTION_INSTALL_LLM.equals(getIntent().getAction())) installLocalLlm();
        if(ACTION_TEST_LLM.equals(getIntent().getAction())) testLocalLlm();
        if(ACTION_TEST_NATIVE.equals(getIntent().getAction())) testNativeLlm();
    }
    private void listen(){ if(checkSelfPermission("android.permission.RECORD_AUDIO")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},42);return;} setState(AssistantState.LISTENING,"LISTENING..."); speech.start(r->{runOnUiThread(()->{if(!r.success){memory.record("<speech>",false,r.error);setState(AssistantState.ERROR,"I couldn't hear that, sir.");return;} setState(AssistantState.PROCESSING,"Heard: "+r.text); CommandPlan p=CommandRouter.parse(r.text); if(!ActionValidator.valid(p)){memory.record(r.text,false,"Unknown command");setState(AssistantState.ERROR,"I couldn't understand that, sir.");return;} if(AgentAccessibilityService.instance==null){setState(AssistantState.ERROR,"AccessibilityService unavailable, sir.");return;} setState(AssistantState.EXECUTING,"EXECUTING..."); ActionResult ar=AgentAccessibilityService.instance.execute(p.actions.get(0));memory.record(r.text,ar.success,ar.message);setState(ar.success?AssistantState.SUCCESS:AssistantState.ERROR,ar.success?"Done, sir.":"I couldn't complete that, sir. "+ar.message); if(tts!=null)tts.speak(status.getText(),TextToSpeech.QUEUE_FLUSH,null,"agent-response");});}); }
    private void startWakeListener(){if(checkSelfPermission("android.permission.RECORD_AUDIO")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},42);return;}setState(AssistantState.IDLE,"IDLE — waiting for GASY");wake.start((phrase,remainder)->runOnUiThread(()->{setState(AssistantState.LISTENING,"GASY DETECTED");if(tts!=null)tts.speak("Yes, sir.",TextToSpeech.QUEUE_FLUSH,null,"wake");if(remainder.isEmpty())listenTextOnly();else routeRecognized(remainder);}));}
    private void listenTextOnly(){setState(AssistantState.LISTENING,"LISTENING...");speech.start(r->{runOnUiThread(()->{if(r.success)routeRecognized(r.text);else setState(AssistantState.ERROR,"I couldn't hear that, sir.");});});}
    private void routeRecognized(String text){CommandPlan p=CommandRouter.parse(text);if(!ActionValidator.valid(p)){setState(AssistantState.ERROR,"I couldn't understand that, sir.");return;}if(AgentAccessibilityService.instance==null){setState(AssistantState.ERROR,"AccessibilityService unavailable, sir.");return;}setState(AssistantState.EXECUTING,"EXECUTING...");ActionResult ar=AgentAccessibilityService.instance.execute(p.actions.get(0));setState(ar.success?AssistantState.SUCCESS:AssistantState.ERROR,ar.success?"Done, sir.":"I couldn't complete that, sir. "+ar.message);if(tts!=null)tts.speak(status.getText(),TextToSpeech.QUEUE_FLUSH,null,"response");}
    private void setState(AssistantState s,String text){state=s;status.setText(text);}
    private void installLocalLlm(){
        Intent i=new Intent("com.termux.RUN_COMMAND"); i.setClassName("com.termux","com.termux.app.RunCommandService");
        i.putExtra("com.termux.RUN_COMMAND_PATH","/data/data/com.termux/files/usr/bin/bash");
        i.putExtra("com.termux.RUN_COMMAND_ARGUMENTS",new String[]{"-lc","pkg update -y && pkg install -y llama-cpp wget && mkdir -p $HOME/models && wget -c -O $HOME/models/qwen2.5-0.5b-instruct-q4_k_m.gguf 'https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true' && pkill llama-server || true; nohup llama-server -m $HOME/models/qwen2.5-0.5b-instruct-q4_k_m.gguf --host 127.0.0.1 --port 8080 >$HOME/llama-server.log 2>&1 &"});
        i.putExtra("com.termux.RUN_COMMAND_BACKGROUND",true); startService(i); status.setText("Termux is installing llama.cpp, sir.");
    }
    private void testLocalLlm(){new Thread(()->{try{String r=new LocalLlmClient().generatePlan("open YouTube");Log.i("AgentHitler","LOCAL_LLM_OK "+r);runOnUiThread(()->status.setText("Local LLM responded, sir."));}catch(Exception e){Log.e("AgentHitler","LOCAL_LLM_FAILED "+e);runOnUiThread(()->status.setText("Local LLM is not ready yet, sir."));}}).start();}
    private void testNativeLlm(){new Thread(()->{try{java.io.File f=new java.io.File(getFilesDir(),"qwen.gguf");if(!f.exists()||f.length()<100000000){try(java.io.InputStream in=getAssets().open("qwen.gguf");java.io.OutputStream out=new java.io.FileOutputStream(f)){byte[] b=new byte[1024*1024];int n;while((n=in.read(b))>0)out.write(b,0,n);}}Log.i("AgentHitler","MODEL_FILE "+f.exists()+" "+f.length()+" "+f.canRead());NativeLlm n=new NativeLlm();boolean ok=n.nativeLoadModel(f.getAbsolutePath());Log.i("AgentHitler","NATIVE_LLM_LOAD "+ok);if(ok){String out=n.nativeGenerate("Return JSON only: {\\\"actions\\\":[{\\\"type\\\":\\\"HOME\\\"}]}",32);Log.i("AgentHitler","NATIVE_LLM_OUTPUT "+out);n.nativeUnloadModel();}runOnUiThread(()->status.setText(ok?"Native local model loaded, sir.":"Native model failed to load, sir."));}catch(Throwable e){Log.e("AgentHitler","NATIVE_LLM_FAILED",e);runOnUiThread(()->status.setText("Native model failed, sir."));}}).start();}
    private void handleTest() {
        CommandPlan plan=CommandRouter.parse("take a screenshot");
        if (!ActionValidator.valid(plan)) { status.setText("I couldn't complete that, sir."); return; }
        if (AgentAccessibilityService.instance == null) { status.setText("Enable AccessibilityService first, sir."); return; }
        ActionResult result=AgentAccessibilityService.instance.execute(plan.actions.get(0));
        status.setText(result.success ? "Done, sir. Test action completed." : "I couldn't complete that, sir. " + result.message);
        if(tts!=null) tts.speak(status.getText(),TextToSpeech.QUEUE_FLUSH,null,"agent-response");
    }
    @Override protected void onDestroy(){if(wake!=null)wake.stop();if(speech!=null)speech.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); if (ACTION_TEST.equals(intent.getAction())) handleTest(); if (ACTION_INSTALL_LLM.equals(intent.getAction())) installLocalLlm(); if (ACTION_TEST_LLM.equals(intent.getAction())) testLocalLlm(); if (ACTION_TEST_NATIVE.equals(intent.getAction())) testNativeLlm(); }
}
