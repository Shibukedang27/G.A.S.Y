package com.agenthitler;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
/**
 * Experimental phrase gate over Android SpeechRecognizer.
 * This is deliberately not presented as a low-power wake-word detector: the
 * recognizer is an ordinary speech session and requires the device recognizer.
 */
public final class WakePhraseListener {
    public static final String GASY = "gasy";
    public interface Callback { void onWake(String phrase, String remainder); }
    private final SpeechRecognizerEngine recognizer; private boolean running; private final Handler handler=new Handler(Looper.getMainLooper()); private int failures;
    public WakePhraseListener(SpeechRecognizerEngine r){recognizer=r;}
    public void start(Callback cb){stop();running=true;failures=0;listen(cb);}
    private void listen(Callback cb){if(!running)return;recognizer.start(result->{if(!running)return;if(result.success){failures=0;String s=result.text.toLowerCase(Locale.US).trim();if(s.equals(GASY)||s.startsWith(GASY+" ")){running=false;recognizer.stop();cb.onWake(GASY,s.substring(GASY.length()).trim());return;}}else failures++;if(running){long delay=Math.min(5000,250L*Math.max(1,failures));handler.postDelayed(()->listen(cb),delay);}});}
    public void stop(){running=false;handler.removeCallbacksAndMessages(null);recognizer.stop();}
}
