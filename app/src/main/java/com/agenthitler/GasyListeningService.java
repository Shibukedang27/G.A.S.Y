package com.agenthitler;

import android.app.*;
import android.content.*;
import android.os.IBinder;

/** Keeps the local microphone loop alive while the UI is backgrounded. */
public final class GasyListeningService extends Service {
    public static final String ACTION_WAKE = "com.agenthitler.GASY_WAKE";
    private WakePhraseListener wake;
    @Override public void onCreate() {
        super.onCreate();
        String channel = "gasy-listening";
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel(channel, "GASY listening", NotificationManager.IMPORTANCE_LOW));
        Notification.Builder b = android.os.Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channel) : new Notification.Builder(this);
        startForeground(42, b.setContentTitle("GASY").setContentText("Listening offline").setSmallIcon(android.R.drawable.ic_btn_speak_now).build());
        wake = new WakePhraseListener(new VoskOfflineSpeechRecognizer(this));
        wake.start((phrase, remainder) -> { Intent i = new Intent(ACTION_WAKE).setPackage(getPackageName()); i.putExtra("remainder", remainder); sendBroadcast(i); });
    }
    @Override public int onStartCommand(Intent intent, int flags, int id) { return START_STICKY; }
    @Override public void onDestroy() { if (wake != null) wake.stop(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
