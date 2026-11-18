package uz.zalefekt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Tizim bo'ylab (audio session 0 = umumiy chiqish) konsert zali effektini qo'llaydi.
 * YouTube, brauzer va boshqa pleyerlar chalayotgan ovozga ta'sir qiladi.
 */
public class EffectService extends Service {

    public static final String ACTION_APPLY = "uz.zalefekt.APPLY";
    public static volatile String status = "Effekt o'chiq";

    private static final String CHANNEL = "zal_fx";

    private SharedPreferences prefs;
    private EffectSet global;
    private final Map<Integer, EffectSet> sessions = new HashMap<>();
    private BroadcastReceiver sessionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("zal", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, "Zal effekti", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }

        // Ba'zi pleyerlar o'z audio sessiyasini e'lon qiladi. Agar umumiy (session 0)
        // effekt ishlamasa, shu sessiyalarga alohida ulanamiz.
        sessionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent in) {
                int sid = in.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1);
                if (sid <= 0) return;
                if (global != null && global.reverbOk) return; // ikki marta qo'llamaslik uchun
                if (AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION.equals(in.getAction())) {
                    if (!sessions.containsKey(sid)) {
                        EffectSet s = new EffectSet();
                        s.create(sid);
                        s.update(prefs);
                        sessions.put(sid, s);
                    }
                } else {
                    EffectSet s = sessions.remove(sid);
                    if (s != null) s.release();
                }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        f.addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        registerReceiver(sessionReceiver, f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, buildNotification());
        apply();
        return START_STICKY;
    }

    private void apply() {
        if (global == null) {
            global = new EffectSet();
            global.create(0);
        }
        global.update(prefs);
        for (EffectSet s : sessions.values()) s.update(prefs);

        status = "Zal: " + (global.reverbOk ? "ulandi" : "YO'Q")
                + "  |  Kenglik: " + (global.virtOk ? "ulandi" : "YO'Q")
                + "  |  Bas: " + (global.bassOk ? "ulandi" : "YO'Q")
                + (global.reverbOk ? "" :
                "\nBu qurilma tizim darajasidagi zal effektini qo'llab-quvvatlamaydi.");
    }

    private Notification buildNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class),
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        b.setContentTitle("Konsert zali effekti yoqilgan")
                .setContentText("Barcha ovoz qayta ishlanmoqda")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setOngoing(true);
        return b.build();
    }

    @Override
    public void onDestroy() {
        try { unregisterReceiver(sessionReceiver); } catch (Throwable ignored) { }
        if (global != null) global.release();
        for (EffectSet s : sessions.values()) s.release();
        sessions.clear();
        global = null;
        status = "Effekt o'chiq";
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Bitta audio sessiyaga ulangan effektlar to'plami. */
    static class EffectSet {
        EnvironmentalReverb env;
        PresetReverb preset;
        Virtualizer virt;
        BassBoost bass;
        boolean reverbOk, virtOk, bassOk;

        void create(int session) {
            try { env = new EnvironmentalReverb(0, session); reverbOk = true; }
            catch (Throwable t) { env = null; }
            if (env == null) {
                try { preset = new PresetReverb(0, session); reverbOk = true; }
                catch (Throwable t) { preset = null; }
            }
            try { virt = new Virtualizer(0, session); virtOk = true; }
            catch (Throwable t) { virt = null; }
            try { bass = new BassBoost(0, session); bassOk = true; }
            catch (Throwable t) { bass = null; }
        }

        void update(SharedPreferences p) {
            int rev = p.getInt("reverb", 60);
            int dec = p.getInt("decay", 50);
            int wid = p.getInt("width", 60);
            int bas = p.getInt("bass", 30);

            try {
                if (env != null) {
                    env.setRoomLevel((short) -1000);
                    env.setRoomHFLevel((short) -100);
                    env.setDecayTime(1500 + dec * 50);          // 1.5 .. 6.5 soniya
                    env.setDecayHFRatio((short) 800);
                    env.setReflectionsLevel((short) -1200);
                    env.setReflectionsDelay(25);
                    env.setReverbLevel((short) (-5000 + rev * 60)); // -50 dB .. +10 dB
                    env.setReverbDelay(50);
                    env.setDiffusion((short) 1000);
                    env.setDensity((short) 1000);
                    env.setEnabled(rev > 0);
                } else if (preset != null) {
                    preset.setPreset(PresetReverb.PRESET_LARGEHALL);
                    preset.setEnabled(rev > 0);
                }
            } catch (Throwable ignored) { }

            try {
                if (virt != null) {
                    virt.setStrength((short) (wid * 10));
                    virt.setEnabled(wid > 0);
                }
            } catch (Throwable ignored) { }

            try {
                if (bass != null) {
                    bass.setStrength((short) (bas * 10));
                    bass.setEnabled(bas > 0);
                }
            } catch (Throwable ignored) { }
        }

        void release() {
            try { if (env != null) env.release(); } catch (Throwable ignored) { }
            try { if (preset != null) preset.release(); } catch (Throwable ignored) { }
            try { if (virt != null) virt.release(); } catch (Throwable ignored) { }
            try { if (bass != null) bass.release(); } catch (Throwable ignored) { }
        }
    }
}
