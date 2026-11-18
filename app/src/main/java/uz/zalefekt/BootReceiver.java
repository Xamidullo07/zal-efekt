package uz.zalefekt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

/** Mashina yoqilganda effektni avtomatik ishga tushiradi (agar yoqilgan bo'lsa). */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences p = context.getSharedPreferences("zal", Context.MODE_PRIVATE);
        if (p.getBoolean("on", false) && p.getBoolean("boot", true)) {
            Intent i = new Intent(context, EffectService.class).setAction(EffectService.ACTION_APPLY);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i);
            else context.startService(i);
        }
    }
}
