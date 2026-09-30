package com.agenthitler;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
/** Phrase gate for the current Android STT path. Dedicated low-power KWS can replace this class later. */
public final class WakePhraseListener {
    public static final String GASY = "gasy";
    public interface Callback { void onWake(String phrase, String remainder); }
    private final SpeechRecognizerEngine recognizer; private boolean running; private final Handler handler=new Handler(Looper.getMainLooper()); private int failures;
    public WakePhraseListener(SpeechRecognizerEngine r){recognizer=r;}
    public void start(Callback cb){stop();running=true;failures=0;listen(cb);}
    private void listen(Callback cb){if(!running)return;recognizer.start(result->{if(!running)return;if(result.success){failures=0;String s=result.text.toLowerCase(Locale.US).trim();if(s.startsWith(GASY)){running=false;recognizer.stop();cb.onWake(GASY,s.substring(GASY.length()).trim());return;}}else failures++;if(running){long delay=Math.min(2000,250L*Math.max(1,failures));handler.postDelayed(()->listen(cb),delay);}});}
    public void stop(){running=false;handler.removeCallbacksAndMessages(null);recognizer.stop();}
}
