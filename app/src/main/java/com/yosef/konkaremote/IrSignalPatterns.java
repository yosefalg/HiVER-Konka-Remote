package com.yosef.konkaremote;

/** Pure-Java Consumer IR timing generators; timings and bit order follow the public MT9653 decoders. */
final class IrSignalPatterns {
    static final int CARRIER_HZ = 38_000;

    static int[] nec(int customerCode, int function) {
        if ((customerCode & ~0xFFFF) != 0) throw new IllegalArgumentException("NEC customer code must be 16-bit");
        if ((function & ~0xFF) != 0) throw new IllegalArgumentException("NEC command must be 8-bit");
        int[] pattern = new int[2 + 32 * 2 + 1];
        int p = 0;
        pattern[p++] = 9000;
        pattern[p++] = 4500;
        int[] bytes = {(customerCode >>> 8) & 0xFF, customerCode & 0xFF, function & 0xFF, (~function) & 0xFF};
        for (int value : bytes) {
            for (int bit = 0; bit < 8; bit++) {
                pattern[p++] = 560;
                pattern[p++] = ((value >>> bit) & 1) != 0 ? 1690 : 560;
            }
        }
        pattern[p] = 560;
        return pattern;
    }

    static int[] konkaStaos(int function) {
        if ((function & ~0xFF) != 0) throw new IllegalArgumentException("Konka command must be 8-bit");
        int frame = (0x02 << 8) | function;
        int[] pattern = new int[2 + 16 * 2 + 1];
        int p = 0;
        pattern[p++] = 3000;
        pattern[p++] = 3000;
        for (int bit = 15; bit >= 0; bit--) {
            pattern[p++] = 500;
            pattern[p++] = ((frame >>> bit) & 1) != 0 ? 2500 : 1500;
        }
        pattern[p] = 4000;
        return pattern;
    }

    private IrSignalPatterns() {}
}
