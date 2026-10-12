package com.yosef.konkaremote;

final class IrSignalPatterns {
    static final int CARRIER_HZ = 38_000;
    static int[] nec(int customerCode, int function) {
        if ((customerCode & ~0xFFFF) != 0 || (function & ~0xFF) != 0) throw new IllegalArgumentException("Invalid NEC frame");
        int[] p = new int[67]; int i = 0;
        p[i++] = 9000; p[i++] = 4500;
        int[] bytes = {(customerCode >>> 8) & 255, customerCode & 255, function & 255, (~function) & 255};
        for (int b : bytes) for (int bit = 0; bit < 8; bit++) { p[i++] = 560; p[i++] = ((b >>> bit) & 1) != 0 ? 1690 : 560; }
        p[i] = 560; return p;
    }
    static int[] konkaStaos(int function) {
        if ((function & ~255) != 0) throw new IllegalArgumentException("Invalid Konka frame");
        int frame = (0x02 << 8) | function; int[] p = new int[35]; int i = 0;
        p[i++] = 3000; p[i++] = 3000;
        for (int bit = 15; bit >= 0; bit--) { p[i++] = 500; p[i++] = ((frame >>> bit) & 1) != 0 ? 2500 : 1500; }
        p[i] = 4000; return p;
    }
    private IrSignalPatterns() {}
}
