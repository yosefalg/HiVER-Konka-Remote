package com.yosef.konkaremote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 15, 29);
    private static final int CARD = Color.rgb(17, 28, 47);
    private static final int CARD_SOFT = Color.rgb(27, 41, 64);
    private static final int TEXT = Color.rgb(248, 250, 252);
    private static final int MUTED = Color.rgb(148, 163, 184);
    private static final int ACCENT = Color.rgb(56, 189, 248);
    private static final int GREEN = Color.rgb(52, 211, 153);
    private static final int RED = Color.rgb(248, 77, 77);

    private IrEngine ir;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private String profile;
    private Map<String, Integer> commands;
    private TextView status;
    private boolean showNumbers;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
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
        root.setPadding(dp(18), dp(14), dp(18), dp(28));
        scroll.addView(root);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = column();
        titles.addView(label("HIVER  •  KONKA", 12, ACCENT, true));
        titles.addView(label("الريموت الذكي", 27, TEXT, true));
        titles.addView(label(RemoteProfiles.title(profile), 12, MUTED, false));
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button devices = button("⌄  الأجهزة", CARD_SOFT, TEXT, 14);
        devices.setOnClickListener(v -> showProfilePicker());
        header.addView(devices, size(112, 48));
        root.addView(header, matchWrap(0, 14));

        boolean emitter = ir.isAvailable();
        boolean ready = emitter && ir.supports38Khz();
        String state = ready ? "●  جاهز للإرسال بالأشعة تحت الحمراء"
                : emitter ? "●  مرسل IR موجود، لكن تردد 38kHz غير معلن"
                : "●  لم يعثر التطبيق على مرسل IR";
        status = label(state, 13, ready ? GREEN : RED, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(11), dp(10), dp(11));
        status.setBackground(roundRect(CARD, 18, ready ? Color.rgb(20, 83, 72) : Color.rgb(100, 35, 45)));
        root.addView(status, matchWrap(0, 16));

        LinearLayout remote = column();
        remote.setPadding(dp(15), dp(18), dp(15), dp(18));
        remote.setBackground(gradientCard());
        remote.setElevation(dp(5));

        LinearLayout quick = row();
        quick.setGravity(Gravity.CENTER);
        quick.addView(circleCommandButton("◉\nالمصدر", "INPUT", CARD_SOFT, 13), size(82, 82));
        addSpace(quick, 20);
        quick.addView(circleCommandButton("⏻", "POWER", RED, 32), size(92, 92));
        addSpace(quick, 20);
        quick.addView(circleCommandButton("⌁\nكتم", "MUTE", CARD_SOFT, 13), size(82, 82));
        remote.addView(quick, matchWrap(0, 20));

        remote.addView(sectionTitle("التحكم السريع"), matchWrap(0, 10));
        LinearLayout controls = row();
        controls.addView(verticalControl("الصوت", "VOL_UP", "VOL_DOWN"), weightedHeight(1, 184, 7));
        controls.addView(verticalControl("القناة", "CH_UP", "CH_DOWN"), weightedHeight(1, 184, 7));
        remote.addView(controls);

        LinearLayout utility = row();
        utility.setGravity(Gravity.CENTER);
        addUtility(utility, "↩\nسابق", "BACK");
        addUtility(utility, "ⓘ\nمعلومات", "INFO");
        addUtility(utility, "☰\nالقائمة", "MENU");
        remote.addView(utility, matchWrap(18, 10));

        LinearLayout modeBar = row();
        Button keypad = button(showNumbers ? "إخفاء الأرقام" : "123  لوحة الأرقام", CARD_SOFT, TEXT, 14);
        keypad.setOnClickListener(v -> { showNumbers = !showNumbers; showRemote(); });
        modeBar.addView(keypad, weightedHeight(1, 50, 6));
        if (commands.containsKey("SLEEP")) {
            modeBar.addView(commandButton("◷  مؤقت النوم", "SLEEP", CARD_SOFT, 14), weightedHeight(1, 50, 6));
        }
        remote.addView(modeBar, matchWrap(2, 0));
        if (showNumbers) addNumberPad(remote);
        root.addView(remote, matchWrap(0, 16));

        TextView hint = label("اضغط مطولاً على الصوت أو القناة للتغيير المستمر", 12, MUTED, false);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint, matchWrap(0, 8));
        TextView footer = label("بدون إنترنت  •  بدون إعلانات  •  Consumer IR حقيقي", 12, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
        setContentView(scroll);
    }

    private View verticalControl(String title, String upKey, String downKey) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(10), dp(12), dp(10), dp(12));
        box.setBackground(roundRect(CARD_SOFT, 25, Color.TRANSPARENT));
        TextView name = label(title, 15, MUTED, true);
        name.setGravity(Gravity.CENTER);
        box.addView(name, matchWrap(0, 8));
        box.addView(repeatingCommandButton("+", upKey, CARD, 29), match(59));
        TextView divider = label("•", 14, MUTED, false);
        divider.setGravity(Gravity.CENTER);
        box.addView(divider, matchWrap(0, 0));
        box.addView(repeatingCommandButton("−", downKey, CARD, 29), match(59));
        return box;
    }

    private void addNumberPad(LinearLayout parent) {
        parent.addView(sectionTitle("لوحة الأرقام"), matchWrap(20, 10));
        for (int start = 1; start <= 7; start += 3) {
            LinearLayout line = row();
            for (int n = start; n < start + 3; n++) {
                line.addView(commandButton(String.valueOf(n), String.valueOf(n), CARD, 21), weightedHeight(1, 54, 6));
            }
            parent.addView(line, matchWrap(0, 10));
        }
        LinearLayout zero = row();
        zero.setGravity(Gravity.CENTER);
        zero.addView(commandButton("0", "0", CARD, 21), size(112, 54));
        parent.addView(zero);
    }

    private void addUtility(LinearLayout line, String text, String key) {
        line.addView(commandButton(text, key, CARD, 12), weightedHeight(1, 64, 5));
    }

    private Button repeatingCommandButton(String text, String key, int color, int textSize) {
        Button b = commandButton(text, key, color, textSize);
        Integer code = commands.get(key);
        if (code == null) return b;
        b.setOnClickListener(null);
        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] held = {false};
        Runnable repeat = new Runnable() {
            @Override public void run() {
                if (!held[0]) return;
                send(code, false);
                handler.postDelayed(this, 165);
            }
        };
        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                held[0] = true;
                send(code, true);
                handler.postDelayed(repeat, 420);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                held[0] = false;
                handler.removeCallbacks(repeat);
                v.performClick();
            }
            return true;
        });
        return b;
    }

    private Button commandButton(String text, String key, int color, int textSize) {
        Button b = button(text, color, TEXT, textSize);
        Integer code = commands.get(key);
        if (code == null) {
            b.setAlpha(.3f);
            b.setEnabled(false);
        } else {
            b.setOnClickListener(v -> send(code, true));
        }
        return b;
    }

    private Button circleCommandButton(String text, String key, int color, int textSize) {
        Button b = commandButton(text, key, color, textSize);
        b.setBackground(circleBackground(color));
        b.setElevation(dp(3));
        return b;
    }

    private boolean send(int function, boolean vibrate) {
        try {
            ir.sendAiwa(function);
            if (vibrate) haptic();
            status.setText("✓  تم إرسال الأمر");
            status.setTextColor(GREEN);
            return true;
        } catch (Exception e) {
            status.setText("تعذر الإرسال: " + e.getMessage());
            status.setTextColor(RED);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void showProfilePicker() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("اختيار التلفزيون")
                .setSingleChoiceItems(new String[]{"HiVER H43F01 — RID 5040-A", "Konka KK-Y199"},
                        RemoteProfiles.PROFILE_5040_B.equals(profile) ? 1 : 0, (dialog, which) -> {
                            select(which == 0 ? RemoteProfiles.PROFILE_5040_A : RemoteProfiles.PROFILE_5040_B);
                            dialog.dismiss();
                        })
                .setPositiveButton("ريموت جوكر", (d, w) -> showJokerPicker())
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showJokerPicker() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("جوكر — فحص التوافق")
                .setMessage("وجّه الهاتف نحو التلفزيون. لن يُحفظ أي ملف إلا بعد تأكيدك أن التلفزيون استجاب.")
                .setItems(new String[]{"اختبار RID 5040-A", "اختبار Konka KK-Y199"}, (d, which) ->
                        testCandidate(which == 0 ? RemoteProfiles.PROFILE_5040_A : RemoteProfiles.PROFILE_5040_B))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void testCandidate(String candidate) {
        if (!send(RemoteProfiles.power(candidate), true)) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("هل استجاب التلفزيون؟")
                .setMessage(RemoteProfiles.title(candidate))
                .setPositiveButton("نعم، احفظ الملف", (d, w) -> select(candidate))
                .setNegativeButton("لا", null)
                .show();
    }

    private void select(String selected) {
        profile = selected;
        commands = RemoteProfiles.commands(profile);
        prefs.edit().putString("profile", profile).apply();
        showNumbers = false;
        showRemote();
    }

    private void haptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(22, 80));
        else vibrator.vibrate(22);
    }

    private TextView sectionTitle(String text) {
        TextView v = label(text, 14, TEXT, true);
        v.setGravity(Gravity.START);
        return v;
    }

    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }

    private TextView label(String text, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private Button button(String text, int bg, int fg, int sp) {
        Button b = new Button(this);
        b.setText(text);
        b.setContentDescription(text.replace("\n", " "));
        b.setTextColor(fg);
        b.setTextSize(sp);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(5), 0, dp(5), 0);
        b.setBackground(roundRect(bg, 19, Color.TRANSPARENT));
        b.setStateListAnimator(null);
        return b;
    }

    private GradientDrawable gradientCard() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(20, 33, 55), Color.rgb(12, 22, 39)});
        d.setCornerRadius(dp(32));
        d.setStroke(dp(1), Color.rgb(42, 58, 82));
        return d;
    }

    private GradientDrawable roundRect(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) d.setStroke(dp(1), stroke);
        return d;
    }

    private GradientDrawable circleBackground(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        d.setStroke(dp(1), color == RED ? Color.rgb(255, 130, 130) : Color.rgb(55, 72, 98));
        return d;
    }

    private LinearLayout.LayoutParams size(int w, int h) { return new LinearLayout.LayoutParams(dp(w), dp(h)); }
    private LinearLayout.LayoutParams match(int h) { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(h)); }
    private LinearLayout.LayoutParams weightedHeight(int weight, int h, int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(h), weight);
        p.setMargins(dp(margin), 0, dp(margin), 0);
        return p;
    }
    private LinearLayout.LayoutParams matchWrap(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }
    private void addSpace(LinearLayout l, int value) { l.addView(new View(this), new LinearLayout.LayoutParams(dp(value), 1)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
