package com.agenthitler;

import android.content.*;

/** Restarts the opted-in foreground listener after a device reboot. */
public final class GasyBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        Intent service = new Intent(context, GasyListeningService.class);
        if (android.os.Build.VERSION.SDK_INT >= 26) context.startForegroundService(service); else context.startService(service);
    }
}
