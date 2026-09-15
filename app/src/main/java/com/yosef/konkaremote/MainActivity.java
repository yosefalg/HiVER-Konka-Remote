package com.yosef.konkaremote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public final class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(17, 24, 39);
    private static final int RED = Color.rgb(220, 38, 38);
    private static final int BG = Color.rgb(243, 244, 246);
    private IrEngine ir;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private String profile;
    private Map<String, Integer> commands;
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(BG);
        ir = new IrEngine(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("konka_remote", MODE_PRIVATE);
        profile = prefs.getString("profile", RemoteProfiles.PROFILE_5040_A);
        commands = RemoteProfiles.commands(profile);
        showRemote();
    }

    private void showRemote() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = column();
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root);

        LinearLayout header = row();
        TextView brand = label("تلفزيون Konka", 24, NAVY, true);
        TextView rid = label(RemoteProfiles.title(profile), 12, Color.GRAY, false);
        LinearLayout titleBox = column();
        titleBox.addView(brand); titleBox.addView(rid);
        header.addView(titleBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button settings = button("اختيار / جوكر", NAVY, Color.WHITE);
        settings.setOnClickListener(v -> showProfilePicker());
        header.addView(settings, size(112, 48));
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        root.addView(header, matchWrap(0, 16));

        status = label(ir.isAvailable() && ir.supports38Khz() ? "●  الأشعة تحت الحمراء جاهزة" : "●  الهاتف لا يعلن عن مرسل IR", 14,
                ir.isAvailable() ? Color.rgb(22,163,74) : RED, true);
        status.setGravity(Gravity.CENTER);
        root.addView(status, matchWrap(0, 14));

        LinearLayout top = row();
        top.setGravity(Gravity.CENTER);
        top.addView(circleCommandButton("⌁\nكتم", "MUTE", Color.WHITE, 14), size(86, 86));
        addSpace(top, 16);
        top.addView(circleCommandButton("⏻", "POWER", Color.WHITE, 34), size(86, 86));
        addSpace(top, 16);
        top.addView(circleCommandButton("▣\nالمصدر", "INPUT", Color.WHITE, 14), size(86, 86));
        root.addView(top, matchWrap(0, 18));

        LinearLayout utility = row();
        utility.setGravity(Gravity.CENTER);
        utility.addView(commandButton("رجوع/سابق", "BACK", Color.WHITE, 12), size(92, 58));
        addSpace(utility, 10);
        utility.addView(commandButton("معلومات", "INFO", Color.WHITE, 14), size(92, 58));
        addSpace(utility, 10);
        utility.addView(commandButton("القائمة", "MENU", Color.WHITE, 14), size(92, 58));
        root.addView(utility, matchWrap(0, 18));

        LinearLayout controls = row(); controls.setGravity(Gravity.CENTER);
        controls.addView(verticalControl("الصوت", "+", "VOL_UP", "−", "VOL_DOWN"), size(132, 180));
        addSpace(controls, 28);
        controls.addView(verticalControl("القناة", "+", "CH_UP", "−", "CH_DOWN"), size(132, 180));
        root.addView(controls, matchWrap(0, 18));

        TextView numTitle = label("لوحة الأرقام", 15, Color.DKGRAY, true);
        numTitle.setGravity(Gravity.CENTER);
        root.addView(numTitle, matchWrap(0, 8));
        for (int start = 1; start <= 7; start += 3) {
            LinearLayout numbers = row(); numbers.setGravity(Gravity.CENTER);
            for (int n = start; n < start + 3; n++) {
                numbers.addView(commandButton(String.valueOf(n), String.valueOf(n), Color.WHITE, 20), size(82, 54));
                if (n < start + 2) addSpace(numbers, 12);
            }
            root.addView(numbers, matchWrap(0, 10));
        }
        LinearLayout zero = row(); zero.setGravity(Gravity.CENTER);
        zero.addView(commandButton("0", "0", Color.WHITE, 20), size(82, 54));
        root.addView(zero, matchWrap(0, 10));

        TextView footer = label("يعمل دون إنترنت • لا إعلانات • لا يجمع أي بيانات", 12, Color.GRAY, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, matchWrap(14, 0));
        setContentView(scroll);
    }

    private View verticalControl(String title, String up, String upKey, String down, String downKey) {
        LinearLayout box = column(); box.setGravity(Gravity.CENTER);
        TextView t = label(title, 14, Color.DKGRAY, true); t.setGravity(Gravity.CENTER); box.addView(t, matchWrap(0, 8));
        box.addView(commandButton(up, upKey, Color.WHITE, 26), match(70));
        TextView line = label("•", 18, Color.LTGRAY, false); line.setGravity(Gravity.CENTER); box.addView(line, matchWrap(0, 0));
        box.addView(commandButton(down, downKey, Color.WHITE, 28), match(70));
        return box;
    }

    private Button commandButton(String text, String key, int color, int textSize) {
        Button b = button(text, color, color == Color.WHITE ? NAVY : Color.WHITE);
        b.setTextSize(textSize);
        Integer code = commands.get(key);
        if (code == null) { b.setAlpha(.38f); b.setEnabled(false); }
        else b.setOnClickListener(v -> send(code));
        return b;
    }

    private Button circleCommandButton(String text, String key, int color, int textSize) {
        Button b = commandButton(text, key, color, textSize);
        b.setBackground(circleBackground(color));
        b.setElevation(dp(3));
        return b;
    }

    private void send(int function) {
        try {
            ir.sendAiwa(function);
            haptic();
            status.setText("✓  تم إرسال الأمر إلى التلفزيون");
            status.setTextColor(Color.rgb(22,163,74));
        } catch (Exception e) {
            status.setText("تعذر الإرسال: " + e.getMessage()); status.setTextColor(RED);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showProfilePicker() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("اختيار التلفزيون")
                .setItems(new String[] {
                        "HiVER H43F01 — RID 5040-A",
                        "Konka KK-Y199",
                        "جوكر: اختبار ملفات التشغيل الموثقة"
                }, (d, which) -> {
                    if (which == 0) select(RemoteProfiles.PROFILE_5040_A);
                    else if (which == 1) select(RemoteProfiles.PROFILE_5040_B);
                    else showJokerPicker();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showJokerPicker() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("ريموت جوكر — فحص آمن")
                .setMessage("وجّه الهاتف نحو التلفزيون واختر ملفاً واحداً. يُرسل زر التشغيل الحقيقي لذلك الملف فقط؛ إذا استجاب التلفزيون يُحفظ الملف.")
                .setItems(new String[] {"اختبار RID 5040-A", "اختبار Konka KK-Y199"}, (d, which) -> {
                    String candidate = which == 0 ? RemoteProfiles.PROFILE_5040_A : RemoteProfiles.PROFILE_5040_B;
                    send(RemoteProfiles.power(candidate));
                    select(candidate);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void select(String selected) {
        profile = selected; commands = RemoteProfiles.commands(profile);
        prefs.edit().putString("profile", profile).apply(); showRemote();
    }

    private void haptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(22, 80)); else vibrator.vibrate(22);
    }

    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    private TextView label(String text, int sp, int color, boolean bold) { TextView v = new TextView(this); v.setText(text); v.setTextSize(sp); v.setTextColor(color); if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private Button button(String text, int bg, int fg) { Button b = new Button(this); b.setText(text); b.setTextColor(fg); b.setTextSize(15); b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setGravity(Gravity.CENTER); b.setPadding(dp(4),0,dp(4),0); b.setBackground(roundRect(bg, 18)); return b; }
    private android.graphics.drawable.GradientDrawable roundRect(int color, int radius) { android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); if (color == Color.WHITE) d.setStroke(dp(1), Color.rgb(229,231,235)); return d; }
    private android.graphics.drawable.GradientDrawable circleBackground(int color) { android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable(); d.setShape(android.graphics.drawable.GradientDrawable.OVAL); d.setColor(color); if (color == Color.WHITE) d.setStroke(dp(1), Color.rgb(229,231,235)); return d; }
    private LinearLayout.LayoutParams size(int w, int h) { return new LinearLayout.LayoutParams(dp(w), dp(h)); }
    private LinearLayout.LayoutParams match(int h) { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(h)); }
    private LinearLayout.LayoutParams matchWrap(int top, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0,dp(top),0,dp(bottom)); return p; }
    private void addSpace(LinearLayout l, int dp) { l.addView(new View(this), new LinearLayout.LayoutParams(dp(dp), 1)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
