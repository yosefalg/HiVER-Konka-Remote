package com.yosef.konkaremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(5, 10, 18);
    private static final int BODY_TOP = Color.rgb(45, 51, 61);
    private static final int BODY_BOTTOM = Color.rgb(18, 22, 29);
    private static final int KEY_TOP = Color.rgb(69, 76, 88);
    private static final int KEY_PRESSED = Color.rgb(18, 21, 27);
    private static final int TEXT = Color.rgb(250, 250, 250);
    private static final int MUTED = Color.rgb(173, 181, 193);
    private static final int CYAN = Color.rgb(36, 211, 238);
    private static final int GREEN = Color.rgb(52, 211, 153);
    private static final int RED = Color.rgb(232, 56, 67);
    private static final int BLUE = Color.rgb(41, 121, 255);

    private IrEngine ir;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private String profile;
    private Map<String, Integer> commands;
    private TextView status;
    private TextView channelDisplay;
    private String typedChannel = "";
    private boolean vibrationEnabled;
    private boolean hardwareKeysEnabled;
    private int repeatDelay;
    private final Handler channelHandler = new Handler(Looper.getMainLooper());
    private int channelSequence;
    private long lastHardwareSend;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ir = new IrEngine(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("konka_remote", MODE_PRIVATE);
        profile = prefs.getString("profile", RemoteProfiles.PROFILE_5040_A);
        commands = RemoteProfiles.commands(profile);
        vibrationEnabled = prefs.getBoolean("vibration", true);
        hardwareKeysEnabled = prefs.getBoolean("hardware_keys", true);
        repeatDelay = prefs.getInt("repeat_delay", 165);
        showRemote();
    }

    private void showRemote() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);

        LinearLayout page = column();
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        // Keep the remote below the system status bar on edge-to-edge devices.
        page.setPadding(dp(14), dp(38), dp(14), dp(40));
        scroll.addView(page);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brand = column();
        brand.addView(label("HIVER", 11, CYAN, true));
        brand.addView(label("SMART IR", 9, MUTED, true));
        header.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button device = miniButton("الأجهزة  ▾", CYAN);
        device.setOnClickListener(v -> showProfilePicker());
        header.addView(device, size(104, 42));
        Button settings = miniButton("⚙", TEXT);
        settings.setContentDescription("الإعدادات");
        settings.setOnClickListener(v -> showSettings());
        LinearLayout.LayoutParams settingsParams = size(48, 42);
        settingsParams.setMargins(dp(8), 0, 0, 0);
        header.addView(settings, settingsParams);
        page.addView(header, constrained(0, 12));

        TextView model = label(RemoteProfiles.title(profile), 12, MUTED, false);
        model.setGravity(Gravity.CENTER);
        page.addView(model, constrained(0, 10));

        status = label(irState(), 12, ir.isAvailable() ? GREEN : RED, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(8), dp(10), dp(8), dp(10));
        status.setBackground(pill(Color.rgb(12, 28, 30), ir.isAvailable() ? Color.rgb(21, 94, 78) : Color.rgb(110, 35, 45)));
        page.addView(status, constrained(0, 18));

        LinearLayout remote = column();
        remote.setGravity(Gravity.CENTER_HORIZONTAL);
        remote.setPadding(dp(22), dp(18), dp(22), dp(28));
        remote.setBackground(remoteBody());
        remote.setElevation(dp(12));
        page.addView(remote, constrained(0, 20));

        addEmitter(remote);

        LinearLayout powerRow = row();
        powerRow.setGravity(Gravity.CENTER);
        powerRow.addView(roundCommand("كتم\nMUTE", "MUTE", 12, KEY_TOP), size(78, 78));
        addSpace(powerRow, 24);
        powerRow.addView(roundCommand("⏻", "POWER", 31, RED), size(94, 94));
        addSpace(powerRow, 24);
        powerRow.addView(roundCommand("المصدر\nINPUT", "INPUT", 11, BLUE), size(78, 78));
        remote.addView(powerRow, matchWrap(5, 20));

        remote.addView(caption("التحكم الرئيسي"), matchWrap(0, 10));
        LinearLayout rockers = row();
        rockers.setGravity(Gravity.CENTER);
        rockers.addView(rocker("VOL", "الصوت", "VOL_UP", "VOL_DOWN"), weightedHeight(1, 176, 7));
        rockers.addView(rocker("CH", "القناة", "CH_UP", "CH_DOWN"), weightedHeight(1, 176, 7));
        remote.addView(rockers, matchWrap(0, 18));

        remote.addView(caption("اتجاهات الشاشة"), matchWrap(0, 8));
        remote.addView(directionPad(), matchWrap(0, 18));

        LinearLayout utility = row();
        utility.setGravity(Gravity.CENTER);
        addUtility(utility, "↩", "رجوع", "BACK");
        addUtility(utility, "☰", "القائمة", "MENU");
        addUtility(utility, "ⓘ", "معلومات", "INFO");
        if (commands.containsKey("SLEEP")) addUtility(utility, "◷", "نوم", "SLEEP");
        remote.addView(utility, matchWrap(0, 22));

        remote.addView(caption("القناة الرقمية"), matchWrap(0, 8));
        LinearLayout displayBar = row();
        displayBar.setGravity(Gravity.CENTER_VERTICAL);
        channelDisplay = label(typedChannel.isEmpty() ? "—" : typedChannel, 24, CYAN, true);
        channelDisplay.setGravity(Gravity.CENTER);
        channelDisplay.setBackground(insetPanel());
        displayBar.addView(channelDisplay, weightedHeight(1, 55, 5));
        Button clear = raisedButton("مسح", 12, KEY_TOP);
        clear.setOnClickListener(v -> { typedChannel = ""; updateDisplay(); });
        displayBar.addView(clear, size(67, 55));
        remote.addView(displayBar, matchWrap(0, 10));

        addNumberPad(remote);

        Button sendChannel = raisedButton("إرسال القناة", 15, BLUE);
        sendChannel.setOnClickListener(v -> sendTypedChannel());
        remote.addView(sendChannel, matchHeight(54, 10, 20));

        remote.addView(caption("القنوات المفضلة  •  اضغط مطولاً للحفظ"), matchWrap(0, 9));
        LinearLayout favorites = row();
        for (int i = 1; i <= 3; i++) favorites.addView(favoriteButton(i), weightedHeight(1, 58, 5));
        remote.addView(favorites, matchWrap(0, 8));

        TextView grip = label("••••••••••••••••", 17, Color.rgb(78, 85, 96), true);
        grip.setGravity(Gravity.CENTER);
        remote.addView(grip, matchWrap(22, 0));

        TextView hint = label("الضغط المطوّل أو أزرار صوت الهاتف للتحكم بالتلفزيون", 11, MUTED, false);
        hint.setGravity(Gravity.CENTER);
        page.addView(hint, constrained(0, 7));
        TextView footer = label("Consumer IR حقيقي  •  بدون إنترنت  •  بدون إعلانات", 10, Color.rgb(111, 121, 137), false);
        footer.setGravity(Gravity.CENTER);
        page.addView(footer, constrained(0, 0));
        setContentView(scroll);
    }

    private void addEmitter(LinearLayout remote) {
        TextView lens = label("IR", 8, Color.rgb(64, 72, 83), true);
        lens.setGravity(Gravity.CENTER);
        GradientDrawable lensBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(3, 4, 6), Color.rgb(36, 5, 11)});
        lensBg.setCornerRadius(dp(8));
        lensBg.setStroke(dp(1), Color.rgb(74, 31, 38));
        lens.setBackground(lensBg);
        remote.addView(lens, size(82, 13));
    }

    private View rocker(String latin, String arabic, String upKey, String downKey) {
        LinearLayout frame = column();
        frame.setGravity(Gravity.CENTER);
        frame.setPadding(dp(7), dp(8), dp(7), dp(8));
        frame.setBackground(insetPanel());
        TextView name = label(latin + "  " + arabic, 12, MUTED, true);
        name.setGravity(Gravity.CENTER);
        frame.addView(name, matchWrap(0, 7));
        frame.addView(repeatingCommandButton("＋", upKey, 27), match(56));
        TextView line = label("•", 11, Color.rgb(95, 104, 117), false);
        line.setGravity(Gravity.CENTER);
        frame.addView(line, matchWrap(0, 0));
        frame.addView(repeatingCommandButton("−", downKey, 29), match(56));
        return frame;
    }

    private View directionPad() {
        LinearLayout pad = column();
        pad.setGravity(Gravity.CENTER);
        LinearLayout top = row();
        top.setGravity(Gravity.CENTER);
        top.addView(commandButton("▲\nفوق", "UP", 13), size(86, 52));
        pad.addView(top, matchWrap(0, 6));
        LinearLayout middle = row();
        middle.setGravity(Gravity.CENTER);
        // Direction keys must read physically left-to-right even in the Arabic UI.
        middle.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        middle.addView(commandButton("◀\nيسار", "LEFT", 12), size(86, 52));
        middle.addView(commandButton("OK", "ENTER", 15), size(72, 52));
        middle.addView(commandButton("يمين\n▶", "RIGHT", 12), size(86, 52));
        pad.addView(middle, matchWrap(0, 6));
        LinearLayout bottom = row();
        bottom.setGravity(Gravity.CENTER);
        bottom.addView(commandButton("▼\nتحت", "DOWN", 13), size(86, 52));
        pad.addView(bottom);
        if (!commands.containsKey("UP")) {
            TextView note = label("الاتجاهات غير متوفرة في ملف Aiwa الحالي — اختر Konka STAOS من الأجهزة للتجربة", 9, Color.rgb(137, 146, 158), false);
            note.setGravity(Gravity.CENTER);
            pad.addView(note, matchWrap(8, 0));
        }
        return pad;
    }

    private void addNumberPad(LinearLayout parent) {
        for (int start = 1; start <= 7; start += 3) {
            LinearLayout line = row();
            for (int n = start; n < start + 3; n++) {
                final String digit = String.valueOf(n);
                Button key = raisedButton(digit, 20, KEY_TOP);
                key.setOnClickListener(v -> appendDigit(digit));
                line.addView(key, weightedHeight(1, 49, 5));
            }
            parent.addView(line, matchWrap(0, 8));
        }
        LinearLayout last = row();
        last.setGravity(Gravity.CENTER);
        Button dash = raisedButton("—", 17, KEY_TOP);
        dash.setEnabled(false);
        dash.setAlpha(.42f);
        last.addView(dash, weightedHeight(1, 49, 5));
        Button zero = raisedButton("0", 20, KEY_TOP);
        zero.setOnClickListener(v -> appendDigit("0"));
        last.addView(zero, weightedHeight(1, 49, 5));
        Button backspace = raisedButton("⌫", 18, KEY_TOP);
        backspace.setOnClickListener(v -> removeDigit());
        last.addView(backspace, weightedHeight(1, 49, 5));
        parent.addView(last, matchWrap(0, 0));
    }

    private Button favoriteButton(int slot) {
        String saved = prefs.getString("favorite_" + slot, "");
        Button b = raisedButton(saved.isEmpty() ? "★ " + slot : "★  " + saved, 14, KEY_TOP);
        b.setContentDescription(saved.isEmpty() ? "المفضلة " + slot + " فارغة" : "القناة المفضلة " + saved);
        b.setOnClickListener(v -> {
            String channel = prefs.getString("favorite_" + slot, "");
            if (channel.isEmpty()) Toast.makeText(this, "اكتب رقم القناة ثم اضغط مطولاً للحفظ", Toast.LENGTH_SHORT).show();
            else sendChannelDigits(channel);
        });
        b.setOnLongClickListener(v -> {
            if (typedChannel.isEmpty()) {
                Toast.makeText(this, "اكتب رقم القناة أولاً", Toast.LENGTH_SHORT).show();
                return true;
            }
            String savedChannel = typedChannel;
            prefs.edit().putString("favorite_" + slot, savedChannel).apply();
            haptic();
            showRemote();
            Toast.makeText(this, "تم حفظ القناة " + savedChannel, Toast.LENGTH_SHORT).show();
            return true;
        });
        return b;
    }

    private void appendDigit(String digit) {
        if (typedChannel.length() >= 4) return;
        typedChannel += digit;
        updateDisplay();
        haptic();
    }

    private void removeDigit() {
        if (!typedChannel.isEmpty()) typedChannel = typedChannel.substring(0, typedChannel.length() - 1);
        updateDisplay();
        haptic();
    }

    private void updateDisplay() {
        if (channelDisplay != null) channelDisplay.setText(typedChannel.isEmpty() ? "—" : typedChannel);
    }

    private void sendTypedChannel() {
        if (typedChannel.isEmpty()) {
            Toast.makeText(this, "اكتب رقم القناة أولاً", Toast.LENGTH_SHORT).show();
            return;
        }
        sendChannelDigits(typedChannel);
    }

    private void sendChannelDigits(String value) {
        if (!ir.isAvailable()) {
            showSendError("لا يوجد مرسل أشعة تحت الحمراء في الهاتف");
            return;
        }
        final int sequence = ++channelSequence;
        channelHandler.removeCallbacksAndMessages(null);
        for (int i = 0; i < value.length(); i++) {
            Integer code = commands.get(String.valueOf(value.charAt(i)));
            if (code == null) continue;
            final int function = code;
            channelHandler.postDelayed(() -> {
                if (sequence == channelSequence) send(function, false);
            }, i * 180L);
        }
        haptic();
    }

    private void addUtility(LinearLayout row, String icon, String title, String key) {
        Button b = commandButton(icon + "\n" + title, key, 11);
        row.addView(b, weightedHeight(1, 62, 4));
    }

    private Button repeatingCommandButton(String text, String key, int textSize) {
        Button b = commandButton(text, key, textSize);
        Integer code = commands.get(key);
        if (code == null) return b;
        b.setOnClickListener(null);
        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] held = {false};
        Runnable repeat = new Runnable() {
            @Override public void run() {
                if (!held[0]) return;
                send(code, false);
                handler.postDelayed(this, repeatDelay);
            }
        };
        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                held[0] = true;
                v.setTranslationY(dp(3));
                v.setElevation(dp(1));
                send(code, true);
                handler.postDelayed(repeat, 390);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                held[0] = false;
                handler.removeCallbacks(repeat);
                v.animate().translationY(0).setDuration(80).start();
                v.setElevation(dp(5));
                v.performClick();
            }
            return true;
        });
        return b;
    }

    private Button commandButton(String text, String key, int textSize) {
        Button b = raisedButton(text, textSize, KEY_TOP);
        Integer code = commands.get(key);
        if (code == null) {
            b.setAlpha(.48f);
            b.setTextColor(Color.rgb(145, 153, 166));
            b.setContentDescription(text.replace("\n", " ") + " — غير متوفر لهذا الموديل");
            b.setEnabled(false);
        } else b.setOnClickListener(v -> send(code, true));
        return b;
    }

    private Button roundCommand(String text, String key, int textSize, int color) {
        Button b = commandButton(text, key, textSize);
        b.setBackground(pressSelector(circleGradient(color, lighten(color)), circleGradient(darken(color), darken(color))));
        b.setElevation(dp(7));
        return b;
    }

    private boolean send(int function, boolean vibrate) {
        return sendForProfile(profile, function, vibrate);
    }

    private boolean sendForProfile(String selectedProfile, int function, boolean vibrate) {
        try {
            if (RemoteProfiles.PROFILE_STAOS.equals(selectedProfile)) ir.sendKonkaStaos(function);
            else ir.sendAiwa(function);
            if (vibrate) haptic();
            status.setText("✓  تم إرسال الأمر");
            status.setTextColor(GREEN);
            return true;
        } catch (Exception e) {
            showSendError(e.getMessage());
            return false;
        }
    }

    private void showSendError(String message) {
        if (status != null) {
            status.setText("تعذر الإرسال: " + message);
            status.setTextColor(RED);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String irState() {
        if (!ir.isAvailable()) return "●  لا يوجد مرسل IR في هذا الهاتف";
        if (!ir.supports38Khz()) return "●  مرسل IR موجود — تردد 38kHz غير معلن";
        return "●  جاهز للإرسال بالأشعة تحت الحمراء";
    }

    private void showSettings() {
        String vibration = vibrationEnabled ? "الاهتزاز: يعمل" : "الاهتزاز: متوقف";
        String hardwareKeys = hardwareKeysEnabled ? "أزرار صوت الهاتف: تتحكم بالتلفزيون" : "أزرار صوت الهاتف: متوقفة";
        String speed = repeatDelay <= 130 ? "سريع" : repeatDelay >= 220 ? "هادئ" : "متوسط";
        new AlertDialog.Builder(this)
                .setTitle("إعدادات الريموت")
                .setItems(new String[]{vibration, hardwareKeys, "سرعة التكرار: " + speed, "مسح القنوات المفضلة", "فحص توافق جوكر"}, (d, which) -> {
                    if (which == 0) {
                        vibrationEnabled = !vibrationEnabled;
                        prefs.edit().putBoolean("vibration", vibrationEnabled).apply();
                        Toast.makeText(this, vibrationEnabled ? "تم تشغيل الاهتزاز" : "تم إيقاف الاهتزاز", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        hardwareKeysEnabled = !hardwareKeysEnabled;
                        prefs.edit().putBoolean("hardware_keys", hardwareKeysEnabled).apply();
                        Toast.makeText(this, hardwareKeysEnabled ? "أزرار الصوت تتحكم بالتلفزيون" : "تم إيقاف تحكم أزرار الصوت", Toast.LENGTH_SHORT).show();
                    } else if (which == 2) showRepeatPicker();
                    else if (which == 3) confirmClearFavorites();
                    else showJokerPicker();
                })
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void showRepeatPicker() {
        int checked = repeatDelay <= 130 ? 0 : repeatDelay >= 220 ? 2 : 1;
        new AlertDialog.Builder(this)
                .setTitle("سرعة الضغط المطوّل")
                .setSingleChoiceItems(new String[]{"سريع", "متوسط", "هادئ"}, checked, (dialog, which) -> {
                    repeatDelay = which == 0 ? 120 : which == 1 ? 165 : 230;
                    prefs.edit().putInt("repeat_delay", repeatDelay).apply();
                    dialog.dismiss();
                }).show();
    }

    private void confirmClearFavorites() {
        new AlertDialog.Builder(this).setTitle("مسح المفضلة؟")
                .setMessage("سيتم حذف القنوات الثلاث المحفوظة فقط.")
                .setPositiveButton("مسح", (d, w) -> {
                    prefs.edit().remove("favorite_1").remove("favorite_2").remove("favorite_3").apply();
                    showRemote();
                }).setNegativeButton("إلغاء", null).show();
    }

    private void showProfilePicker() {
        new AlertDialog.Builder(this)
                .setTitle("اختيار التلفزيون")
                .setSingleChoiceItems(new String[]{"HiVER H43F01 — RID 5040-A", "Konka KK-Y199", "Konka STAOS — اتجاهات وOK"},
                        RemoteProfiles.PROFILE_5040_B.equals(profile) ? 1 : RemoteProfiles.PROFILE_STAOS.equals(profile) ? 2 : 0, (dialog, which) -> {
                            select(which == 0 ? RemoteProfiles.PROFILE_5040_A : which == 1 ? RemoteProfiles.PROFILE_5040_B : RemoteProfiles.PROFILE_STAOS);
                            dialog.dismiss();
                        })
                .setPositiveButton("ريموت جوكر", (d, w) -> showJokerPicker())
                .setNeutralButton("فحص يدوي للملفات", (d, w) -> showManualScan(0))
                .setNegativeButton("إلغاء", null).show();
    }

    /**
     * Guided offline scan of documented profiles; nothing is saved without acceptance.
     * The phone has an IR transmitter but no IR receiver, so the app deliberately
     * asks the user to confirm the TV response instead of pretending to detect it.
     */
    private void showManualScan(int index) {
        final String[] candidates = {RemoteProfiles.PROFILE_5040_A,
                RemoteProfiles.PROFILE_5040_B, RemoteProfiles.PROFILE_STAOS};
        if (index >= candidates.length) {
            new AlertDialog.Builder(this).setTitle("اكتمل الفحص")
                    .setMessage("لم يتم اعتماد أي ملف. لا يستطيع الهاتف قياس استجابة الأشعة؛ التأكيد يتم من تغيّر الشاشة فقط. يمكنك إعادة الفحص أو اختيار ملف من الأجهزة.")
                    .setPositiveButton("حسناً", null).show();
            return;
        }
        String candidate = candidates[index];
        if (!sendForProfile(candidate, RemoteProfiles.power(candidate), true)) return;
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(4), dp(20), dp(4));
        TextView file = label("الملف الحالي: Konka / HiVER", 16, TEXT, true);
        TextView name = label("المرشح: " + RemoteProfiles.title(candidate), 14, MUTED, false);
        TextView warning = label("⚠ تأكيد الاستجابة بصري من التلفاز؛ الهاتف لا يملك مستقبلاً لقياس IR.", 13, Color.rgb(255, 174, 72), true);
        warning.setGravity(Gravity.CENTER);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(20); progress.setProgress(20);
        TextView timerText = label("جاهز — راقب الشاشة الآن", 14, CYAN, true);
        Button confirm = raisedButton("✓  استجاب التلفاز — حفظ الملف", 16, GREEN);
        confirm.setEnabled(true);
        Button next = raisedButton("التالي بعد 20 ثانية", 15, KEY_TOP);
        next.setEnabled(false);
        Button restart = raisedButton("إلغاء وإعادة الفحص", 14, RED);
        content.addView(file); content.addView(name); content.addView(warning);
        content.addView(progress, new LinearLayout.LayoutParams(-1, dp(28)));
        content.addView(timerText); content.addView(confirm, new LinearLayout.LayoutParams(-1, dp(62)));
        content.addView(next, new LinearLayout.LayoutParams(-1, dp(54)));
        content.addView(restart, new LinearLayout.LayoutParams(-1, dp(50)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("فحص يدوي " + (index + 1) + " من " + candidates.length)
                .setView(content).setNegativeButton("إغلاق", null).create();
        confirm.setOnClickListener(v -> { dialog.dismiss(); select(candidate); });
        next.setOnClickListener(v -> { dialog.dismiss(); showManualScan(index + 1); });
        restart.setOnClickListener(v -> { dialog.dismiss(); showManualScan(0); });
        CountDownTimer timer = new CountDownTimer(20000, 1000) {
            public void onTick(long left) { int sec = (int)((left + 999) / 1000); progress.setProgress(sec); timerText.setText("الانتظار قبل المرشح التالي: " + sec + " ثانية"); next.setText("التالي (" + sec + ")"); }
            public void onFinish() { progress.setProgress(0); timerText.setText("انتهى الانتظار — يمكنك متابعة الفحص"); next.setText("التالي"); next.setEnabled(true); }
        };
        dialog.setOnDismissListener(d -> timer.cancel());
        dialog.setOnShowListener(d -> timer.start());
        dialog.show();
    }

    private void showJokerPicker() {
        new AlertDialog.Builder(this).setTitle("جوكر — فحص التوافق")
                .setMessage("وجّه الهاتف نحو التلفزيون. لا يُحفظ الملف إلا بعد تأكيد استجابة التلفزيون.")
                .setItems(new String[]{"اختبار RID 5040-A", "اختبار Konka KK-Y199", "اختبار Konka STAOS مع الاتجاهات"}, (d, which) ->
                        testCandidate(which == 0 ? RemoteProfiles.PROFILE_5040_A : which == 1 ? RemoteProfiles.PROFILE_5040_B : RemoteProfiles.PROFILE_STAOS))
                .setNegativeButton("إلغاء", null).show();
    }

    private void testCandidate(String candidate) {
        if (!sendForProfile(candidate, RemoteProfiles.power(candidate), true)) return;
        new AlertDialog.Builder(this).setTitle("هل استجاب التلفزيون؟")
                .setMessage(RemoteProfiles.title(candidate))
                .setPositiveButton("نعم، احفظ الملف", (d, w) -> select(candidate))
                .setNegativeButton("لا", null).show();
    }

    private void select(String selected) {
        channelSequence++;
        channelHandler.removeCallbacksAndMessages(null);
        profile = selected;
        commands = RemoteProfiles.commands(profile);
        prefs.edit().putString("profile", profile).apply();
        typedChannel = "";
        showRemote();
    }

    private void haptic() {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(18, 70));
        else vibrator.vibrate(18);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hardwareKeysEnabled && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            Integer function = commands.get(keyCode == KeyEvent.KEYCODE_VOLUME_UP ? "VOL_UP" : "VOL_DOWN");
            long now = android.os.SystemClock.elapsedRealtime();
            if (function != null && (event.getRepeatCount() == 0 || now - lastHardwareSend >= repeatDelay)) {
                lastHardwareSend = now;
                send(function, event.getRepeatCount() == 0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (hardwareKeysEnabled && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override protected void onDestroy() {
        channelSequence++;
        channelHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private TextView caption(String text) {
        TextView v = label(text, 12, MUTED, true);
        v.setGravity(Gravity.START);
        return v;
    }

    private Button miniButton(String text, int color) {
        Button b = raisedButton(text, 12, Color.rgb(24, 30, 40));
        b.setTextColor(color);
        return b;
    }

    private Button raisedButton(String text, int sp, int topColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setContentDescription(text.replace("\n", " "));
        b.setTextColor(TEXT);
        b.setTextSize(sp);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setBackground(pressSelector(keyGradient(topColor, darken(topColor)), keyGradient(KEY_PRESSED, Color.BLACK)));
        b.setElevation(dp(5));
        b.setStateListAnimator(null);
        // A short scale/elevation response makes every key feel like a physical remote.
        b.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(.96f).scaleY(.96f).setDuration(60).start();
                v.setElevation(dp(2));
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                v.setElevation(dp(5));
            }
            return false;
        });
        return b;
    }

    private StateListDrawable pressSelector(GradientDrawable normal, GradientDrawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, normal);
        return states;
    }

    private GradientDrawable remoteBody() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{BODY_TOP, BODY_BOTTOM, Color.rgb(9, 12, 17)});
        d.setCornerRadii(new float[]{dp(45), dp(45), dp(45), dp(45), dp(58), dp(58), dp(58), dp(58)});
        d.setStroke(dp(2), Color.rgb(73, 82, 96));
        return d;
    }

    private GradientDrawable keyGradient(int top, int bottom) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{lighten(top), bottom});
        d.setCornerRadius(dp(16));
        d.setStroke(dp(1), Color.rgb(85, 93, 105));
        return d;
    }

    private GradientDrawable circleGradient(int bottom, int top) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{top, bottom});
        d.setShape(GradientDrawable.OVAL);
        d.setStroke(dp(2), lighten(bottom));
        return d;
    }

    private GradientDrawable insetPanel() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(10, 13, 18), Color.rgb(27, 32, 41)});
        d.setCornerRadius(dp(18));
        d.setStroke(dp(1), Color.rgb(58, 65, 76));
        return d;
    }

    private GradientDrawable pill(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(24));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private int lighten(int color) {
        return Color.rgb(Math.min(255, Color.red(color) + 28), Math.min(255, Color.green(color) + 28), Math.min(255, Color.blue(color) + 28));
    }

    private int darken(int color) {
        return Color.rgb(Math.max(0, Color.red(color) - 24), Math.max(0, Color.green(color) - 24), Math.max(0, Color.blue(color) - 24));
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

    private LinearLayout.LayoutParams size(int w, int h) { return new LinearLayout.LayoutParams(dp(w), dp(h)); }
    private LinearLayout.LayoutParams match(int h) { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(h)); }
    private LinearLayout.LayoutParams matchHeight(int h, int top, int bottom) {
        LinearLayout.LayoutParams p = match(h); p.setMargins(0, dp(top), 0, dp(bottom)); return p;
    }
    private LinearLayout.LayoutParams weightedHeight(int weight, int h, int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(h), weight);
        p.setMargins(dp(margin), 0, dp(margin), 0);
        return p;
    }
    private LinearLayout.LayoutParams matchWrap(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom)); return p;
    }
    private LinearLayout.LayoutParams constrained(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom)); return p;
    }
    private void addSpace(LinearLayout l, int value) { l.addView(new View(this), new LinearLayout.LayoutParams(dp(value), 1)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
