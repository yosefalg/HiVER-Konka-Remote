package com.yosef.konkaremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;

final class IrEngine {
    private static final int FREQUENCY = 38_000;
    private final ConsumerIrManager manager;

    IrEngine(Context context) {
        manager = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
    }

    boolean isAvailable() {
        try {
            return manager != null && manager.hasIrEmitter();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    boolean supports38Khz() {
        if (!isAvailable()) return false;
        ConsumerIrManager.CarrierFrequencyRange[] ranges;
        try {
            ranges = manager.getCarrierFrequencies();
        } catch (RuntimeException ignored) {
            // Some vendor IR HALs do not expose frequency ranges even though transmit works.
            return true;
        }
        if (ranges == null || ranges.length == 0) return true;
        for (ConsumerIrManager.CarrierFrequencyRange range : ranges) {
            if (range.getMinFrequency() <= FREQUENCY && range.getMaxFrequency() >= FREQUENCY) return true;
        }
        return false;
    }

    void sendAiwa(int function) {
        if (!isAvailable()) throw new IllegalStateException("لا يوجد مرسل أشعة تحت الحمراء في الهاتف");
        // Public LIRC/IRDB Konka profiles: Aiwa protocol, device 25, subdevice 1.
        long preData = 0x2620CEFL;
        int command = ((function & 0xFF) << 8) | ((~function) & 0xFF);
        int[] pattern = new int[2 + (26 + 16) * 2 + 1];
        int p = 0;
        pattern[p++] = 9010;
        pattern[p++] = 4430;
        for (int bit = 25; bit >= 0; bit--) {
            pattern[p++] = 618;
            pattern[p++] = ((preData >>> bit) & 1L) == 1L ? 1630 : 505;
        }
        for (int bit = 15; bit >= 0; bit--) {
            pattern[p++] = 618;
            pattern[p++] = ((command >>> bit) & 1) == 1 ? 1630 : 505;
        }
        pattern[p] = 618;
        try {
            manager.transmit(FREQUENCY, pattern);
        } catch (SecurityException e) {
            throw new IllegalStateException("تعذر الوصول إلى مرسل الأشعة تحت الحمراء في الهاتف");
        }
    }

    /** Public MStar/MediaTek Konka STAOS protocol used by the optional STAOS profile. */
    void sendKonkaStaos(int function) {
        if (!isAvailable()) throw new IllegalStateException("لا يوجد مرسل أشعة تحت الحمراء في الهاتف");
        // IR_KONKA.h: 3ms header pulse/space, 16 data bits, 500us pulse,
        // 1500us for 0, 2500us for 1, and 4ms trailer space.
        int frame = (0x02 << 8) | (function & 0xFF);
        int[] pattern = new int[2 + (16 * 2) + 1];
        int p = 0;
        pattern[p++] = 3000;
        pattern[p++] = 3000;
        for (int bit = 15; bit >= 0; bit--) {
            pattern[p++] = 500;
            pattern[p++] = ((frame >>> bit) & 1) == 1 ? 2500 : 1500;
        }
        pattern[p] = 4000;
        try {
            manager.transmit(FREQUENCY, pattern);
        } catch (SecurityException e) {
            throw new IllegalStateException("تعذر الوصول إلى مرسل الأشعة تحت الحمراء في الهاتف");
        }
    }
}
