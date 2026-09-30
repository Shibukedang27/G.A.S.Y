package com.agenthitler;

import android.content.Context;
import android.util.Log;
import java.io.*;

/** Runs the bundled Qwen model locally and returns only validated action plans. */
public final class LocalActionPlanner {
    private final Context context;
    private NativeLlm llm;
    public LocalActionPlanner(Context c) { context = c.getApplicationContext(); }
    public synchronized CommandPlan plan(String command) throws IOException {
        if (llm == null) {
            File model = new File(context.getFilesDir(), "qwen.gguf");
            if (!model.exists() || model.length() < 100_000_000L) copyModel(model);
            llm = new NativeLlm();
            boolean loaded = llm.nativeLoadModel(model.getAbsolutePath());
            Log.i("GASY", "LOCAL_MODEL_LOAD=" + loaded + " bytes=" + model.length());
            if (!loaded) throw new IOException("local model failed to load");
        }
        String prompt = "You are GASY. Return JSON only, no markdown. Approved action types: HOME, BACK, RECENTS, OPEN_APP, OPEN_URL, SEARCH_WEB, OPEN_SETTINGS, VOLUME_UP, VOLUME_DOWN, VOLUME_MUTE, PLAY_MEDIA, PAUSE_MEDIA, NEXT_MEDIA, PREVIOUS_MEDIA, SCROLL, TAKE_SCREENSHOT. Return exactly {\"actions\":[{\"type\":\"...\"}]}. Command: " + command;
        String raw = llm.nativeGenerate(prompt, 128);
        Log.i("GASY", "LOCAL_PLAN_RAW_LEN=" + (raw == null ? -1 : raw.length()) + " " + (raw == null ? "null" : raw.replace("\n", "\\n")));
        CommandPlan result = ActionJsonParser.parse(raw);
        if (result == null) { raw = llm.nativeGenerate(prompt + " Do not explain anything.", 128); Log.i("GASY", "LOCAL_PLAN_RETRY_RAW_LEN=" + (raw == null ? -1 : raw.length()) + " " + (raw == null ? "null" : raw.replace("\n", "\\n"))); result = ActionJsonParser.parse(raw); }
        if (result == null) throw new IOException("model returned invalid action JSON");
        return result;
    }
    private void copyModel(File out) throws IOException {
        try (InputStream in = context.getAssets().open("qwen.gguf"); OutputStream os = new FileOutputStream(out)) {
            byte[] b = new byte[1024 * 1024]; int n; while ((n = in.read(b)) != -1) os.write(b, 0, n);
        }
    }
    public synchronized void close() { if (llm != null) { llm.nativeUnloadModel(); llm = null; } }
}
