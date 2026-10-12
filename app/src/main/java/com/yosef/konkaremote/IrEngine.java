package com.yosef.konkaremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;

final class IrEngine {
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
        try {
            ConsumerIrManager.CarrierFrequencyRange[] ranges = manager.getCarrierFrequencies();
            if (ranges == null || ranges.length == 0) return true;
            for (ConsumerIrManager.CarrierFrequencyRange range : ranges) {
                if (range.getMinFrequency() <= IrSignalPatterns.CARRIER_HZ
                        && range.getMaxFrequency() >= IrSignalPatterns.CARRIER_HZ) return true;
            }
            return false;
        } catch (RuntimeException ignored) {
            // Some vendor IR HALs do not report ranges although transmit is available.
            return true;
        }
    }

    void sendNec(int customerCode, int function) {
        transmit(IrSignalPatterns.nec(customerCode, function));
    }

    /** Public MediaTek Konka STAOS protocol: customer byte 0x02 plus the mapped command. */
    void sendKonkaStaos(int function) {
        transmit(IrSignalPatterns.konkaStaos(function));
    }

    private void transmit(int[] pattern) {
        if (!isAvailable()) throw new IllegalStateException("لا يوجد مرسل أشعة تحت الحمراء في الهاتف");
        if (!supports38Khz()) throw new IllegalStateException("هذا الهاتف لا يعلن دعم تردد 38kHz؛ أوقف الإرسال بدلاً من إرسال تردد غير مدعوم");
        try {
            manager.transmit(IrSignalPatterns.CARRIER_HZ, pattern);
        } catch (SecurityException e) {
            throw new IllegalStateException("تعذر الوصول إلى مرسل الأشعة تحت الحمراء في الهاتف");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("رفض نظام الهاتف نمط إرسال الأشعة تحت الحمراء", e);
        }
    }
}
