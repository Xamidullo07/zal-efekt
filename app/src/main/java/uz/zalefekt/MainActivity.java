package uz.zalefekt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private TextView statusView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            statusView.setText(EffectService.status);
            handler.postDelayed(this, 800);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("zal", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Konsert zali effekti");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("YouTube va boshqa dasturlardagi ovozga ham ta'sir qiladi");
        sub.setTextSize(15);
        sub.setPadding(0, dp(4), 0, dp(20));
        root.addView(sub);

        Switch sw = new Switch(this);
        sw.setText("Effekt yoqilgan  ");
        sw.setTextSize(22);
        sw.setChecked(prefs.getBoolean("on", false));
        sw.setPadding(0, dp(8), 0, dp(16));
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("on", checked).apply();
            if (checked) startFx();
            else stopService(new Intent(this, EffectService.class));
        });
        root.addView(sw);

        addSlider(root, "Zal kuchi", "reverb", 60);
        addSlider(root, "Zal uzunligi (aks-sado)", "decay", 50);
        addSlider(root, "Stereo kengligi (3D)", "width", 60);
        addSlider(root, "Bas", "bass", 30);

        CheckBox boot = new CheckBox(this);
        boot.setText("Mashina yoqilganda avtomatik ishga tushsin");
        boot.setTextSize(17);
        boot.setChecked(prefs.getBoolean("boot", true));
        boot.setOnCheckedChangeListener((b, c) -> prefs.edit().putBoolean("boot", c).apply());
        boot.setPadding(0, dp(16), 0, dp(16));
        root.addView(boot);

        statusView = new TextView(this);
        statusView.setTextSize(15);
        statusView.setTextColor(Color.LTGRAY);
        root.addView(statusView);

        setContentView(scroll);

        if (prefs.getBoolean("on", false)) startFx();
    }

    private void addSlider(LinearLayout root, String label, final String key, int def) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(18);
        t.setPadding(0, dp(14), 0, 0);
        root.addView(t);

        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(prefs.getInt(key, def));
        sb.setPadding(dp(8), dp(12), dp(8), dp(12));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (!fromUser) return;
                prefs.edit().putInt(key, p).apply();
                if (prefs.getBoolean("on", false)) startFx();
            }

            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { }
        });
        root.addView(sb);
    }

    private void startFx() {
        Intent i = new Intent(this, EffectService.class).setAction(EffectService.ACTION_APPLY);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(poll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
    }
}
