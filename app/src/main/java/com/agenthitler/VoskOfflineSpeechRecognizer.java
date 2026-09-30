package com.agenthitler;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import org.vosk.Model;
import org.vosk.Recognizer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

/** Fully local STT engine. The model is unpacked once into app-private storage. */
public final class VoskOfflineSpeechRecognizer implements SpeechRecognizerEngine {
    private final Context context;
    private volatile boolean running;
    private AudioRecord recorder;
    private Thread worker;
    private Model model;

    public VoskOfflineSpeechRecognizer(Context c) { context = c.getApplicationContext(); }

    @Override public synchronized void start(Callback cb) {
        stop();
        worker = new Thread(() -> {
            try {
                if (model == null) model = new Model(unpackModel());
                int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min * 2, 8192));
                Recognizer recognizer = new Recognizer(model, 16000);
                byte[] buffer = new byte[Math.max(min, 4096)];
                running = true; recorder.startRecording();
                Log.i("GASY", "Vosk offline STT ready");
                long deadline = System.currentTimeMillis() + 6000;
                while (running) {
                    int n = recorder.read(buffer, 0, buffer.length);
                    if (n > 0 && recognizer.acceptWaveForm(buffer, n)) {
                        String text = extract(recognizer.getResult());
                        if (!text.isEmpty()) { cb.onResult(SpeechResult.ok(text)); break; }
                    }
                    if (System.currentTimeMillis() >= deadline) {
                        String text = extract(recognizer.getFinalResult());
                        Log.i("GASY", "Vosk final transcript: " + text);
                        cb.onResult(text.isEmpty() ? SpeechResult.fail("No offline speech recognized") : SpeechResult.ok(text));
                        break;
                    }
                }
                recognizer.close();
            } catch (Throwable t) { Log.e("GASY", "Vosk offline STT failed", t); cb.onResult(SpeechResult.fail("Offline STT failed: " + t.getClass().getSimpleName())); }
            finally { stop(); }
        }, "gasy-vosk-stt");
        worker.start();
    }

    private String unpackModel() throws IOException {
        File root = new File(context.getFilesDir(), "vosk-model-small-en-us-0.15");
        File marker = new File(root, ".ready");
        if (!marker.exists()) { copyTree("vosk-model-small-en-us-0.15", root); if (!marker.createNewFile()) throw new IOException("model marker"); }
        return root.getAbsolutePath();
    }
    private void copyTree(String asset, File out) throws IOException {
        String[] children = context.getAssets().list(asset);
        if (children == null || children.length == 0) { out.getParentFile().mkdirs(); try (InputStream in = context.getAssets().open(asset); OutputStream os = new FileOutputStream(out)) { byte[] b = new byte[8192]; int n; while ((n = in.read(b)) != -1) os.write(b, 0, n); } return; }
        if (!out.exists() && !out.mkdirs()) throw new IOException("mkdir " + out);
        for (String child : children) copyTree(asset + "/" + child, new File(out, child));
    }
    private static String extract(String json) { Matcher m = Pattern.compile("\\\"text\\\"\\s*:\\s*\\\"([^\"]*)").matcher(json); return m.find() ? m.group(1).trim() : ""; }
    @Override public synchronized void stop() { running = false; if (recorder != null) { try { recorder.stop(); } catch (Throwable ignored) {} recorder.release(); recorder = null; } }
    @Override public synchronized void destroy() { stop(); if (model != null) { model.close(); model = null; } }
}
