package com.yosef.konkaremote;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Command maps derived from the public MediaTek MT9653 Linux IR keymaps. */
final class RemoteProfiles {
    static final String PROFILE_STAOS = "konka_staos";
    static final String PROFILE_CHANGHONG = "changhong_nec";
    static final String PROFILE_HISENSE = "hisense_nec";
    static final String PROFILE_MTK = "mtk_nec";
    static final String DEFAULT_PROFILE = PROFILE_STAOS;

    static String normalize(String profile) {
        if (PROFILE_STAOS.equals(profile) || PROFILE_CHANGHONG.equals(profile)
                || PROFILE_HISENSE.equals(profile) || PROFILE_MTK.equals(profile)) return profile;
        // Older releases mislabeled generic Aiwa profiles as RID 5040. They are not retained.
        if ("rid5040_a".equals(profile) || "rid5040_b".equals(profile)
                || "konka_kk_y250a".equals(profile) || "konka_kk_y199".equals(profile)) return DEFAULT_PROFILE;
        return DEFAULT_PROFILE;
    }

    static String[] allProfiles() {
        return new String[]{PROFILE_STAOS, PROFILE_CHANGHONG, PROFILE_HISENSE, PROFILE_MTK};
    }

    static boolean usesKonkaProtocol(String profile) {
        return PROFILE_STAOS.equals(normalize(profile));
    }

    /** Full 16-bit NEC customer code as published by the upstream kernel keymap. */
    static int necCustomerCode(String profile) {
        switch (normalize(profile)) {
            case PROFILE_CHANGHONG: return 0x40BF;
            case PROFILE_HISENSE: return 0x00BF;
            case PROFILE_MTK: return 0x807F;
            default: throw new IllegalArgumentException("This profile does not use NEC");
        }
    }

    static Map<String, Integer> commands(String profile) {
        String selected = normalize(profile);
        Map<String, Integer> c = new HashMap<>();
        if (PROFILE_STAOS.equals(selected)) {
            // keymap-konka-tv.c: Konka STAOS map, customer code 0x0002.
            put(c, "POWER",0x0B,"MUTE",0x14,"CH_UP",0x11,"CH_DOWN",0x10,"VOL_UP",0x13,"VOL_DOWN",0x12,
                    "UP",0x2B,"DOWN",0x2C,"LEFT",0x2D,"RIGHT",0x2E,"ENTER",0x2F,
                    "MENU",0x15,"INPUT",0x1C,"BACK",0x30,"INFO",0x0A,"HOME",0x38,
                    "0",0,"1",1,"2",2,"3",3,"4",4,"5",5,"6",6,"7",7,"8",8,"9",9,
                    "RED",0x1A,"GREEN",0x1F,"YELLOW",0x0C,"BLUE",0x16);
        } else if (PROFILE_CHANGHONG.equals(selected)) {
            // keymap-changhong-tv.c: NEC customer code 0x40BF.
            put(c, "POWER",0x12,"MUTE",0x10,"CH_UP",0x1B,"CH_DOWN",0x1F,"VOL_UP",0x1A,"VOL_DOWN",0x1E,
                    "UP",0x19,"DOWN",0x1D,"LEFT",0x46,"RIGHT",0x47,"ENTER",0x0A,
                    "MENU",0x5B,"INPUT",0x53,"BACK",0x40,"INFO",0x16,"HOME",0x51,
                    "0",0,"1",1,"2",2,"3",3,"4",4,"5",5,"6",6,"7",7,"8",8,"9",9);
        } else if (PROFILE_HISENSE.equals(selected)) {
            // keymap-hisense-tv.c: NEC customer code 0x00BF.
            put(c, "POWER",0x0D,"MUTE",0x0E,"CH_UP",0x4A,"CH_DOWN",0x4B,"VOL_UP",0x44,"VOL_DOWN",0x43,
                    "UP",0x16,"DOWN",0x17,"LEFT",0x19,"RIGHT",0x18,"ENTER",0x15,
                    "MENU",0x14,"INPUT",0x12,"BACK",0x48,"INFO",0x95,"HOME",0x94,"SLEEP",0x1A,
                    "0",0,"1",1,"2",2,"3",3,"4",4,"5",5,"6",6,"7",7,"8",8,"9",9);
        } else {
            // keymap-mtk-tv.c: generic MediaTek TV NEC customer code 0x807F.
            put(c, "POWER",0x46,"MUTE",0x5A,"CH_UP",0x1F,"CH_DOWN",0x19,"VOL_UP",0x16,"VOL_DOWN",0x15,
                    "UP",0x52,"DOWN",0x13,"LEFT",0x06,"RIGHT",0x1A,"ENTER",0x0F,
                    "MENU",0x07,"INPUT",0x04,"BACK",0x1B,"INFO",0x14,"HOME",0x17,"SLEEP",0x45,
                    "0",0x50,"1",0x49,"2",0x55,"3",0x59,"4",0x4D,"5",0x51,"6",0x5D,"7",0x48,"8",0x54,"9",0x58);
        }
        return Collections.unmodifiableMap(c);
    }

    static int power(String profile) {
        Integer value = commands(profile).get("POWER");
        if (value == null) throw new IllegalArgumentException("Profile has no power command");
        return value;
    }

    static String title(String profile) {
        switch (normalize(profile)) {
            case PROFILE_STAOS: return "Konka STAOS • مصدر MediaTek Linux";
            case PROFILE_CHANGHONG: return "Changhong TV • NEC 0x40BF";
            case PROFILE_HISENSE: return "Hisense TV • NEC 0x00BF";
            default: return "MediaTek Smart TV • NEC 0x807F";
        }
    }

    private static void put(Map<String, Integer> map, Object... pairs) {
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], (Integer) pairs[i + 1]);
    }

    private RemoteProfiles() {}
}
