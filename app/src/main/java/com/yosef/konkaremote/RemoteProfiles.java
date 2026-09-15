package com.yosef.konkaremote;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class RemoteProfiles {
    static final String PROFILE_5040_A = "rid5040_a";
    static final String PROFILE_5040_B = "rid5040_b";
    static final String PROFILE_STAOS = "konka_staos";

    static Map<String, Integer> commands(String profile) {
        Map<String, Integer> c = new HashMap<>();
        if (PROFILE_STAOS.equals(profile)) {
            // Public MStar/MediaTek Konka STAOS driver map (IR_KONKA.h):
            // 16-bit Konka frame, custom 0x02/0x00, including navigation keys.
            put(c, "POWER",0x46,"MUTE",0x5A,"CH_UP",0x1F,"CH_DOWN",0x19,"VOL_UP",0x13,"VOL_DOWN",0x12,
                    "UP",0x2B,"DOWN",0x2C,"LEFT",0x2D,"RIGHT",0x2E,"ENTER",0x2F,
                    "MENU",0x15,"INPUT",0x04,"BACK",0x30,"INFO",0x14,"SLEEP",0x45,
                    "0",0x50,"1",0x49,"2",0x55,"3",0x59,"4",0x4D,"5",0x51,"6",0x5D,"7",0x48,"8",0x54,"9",0x58);
        } else if (PROFILE_5040_B.equals(profile)) {
            // Public IRDB profile: Konka KK-Y199, Aiwa device 25/subdevice 1.
            put(c, "POWER",1,"MUTE",10,"CH_UP",2,"CH_DOWN",3,"VOL_UP",4,"VOL_DOWN",5,
                    "MENU",29,"INPUT",14,"BACK",12,"INFO",11,"SLEEP",13,
                    "0",16,"1",17,"2",18,"3",19,"4",20,"5",21,"6",22,"7",23,"8",24,"9",25);
        } else {
            // Public IRDB profile: Konka KK-Y250A, Aiwa device 25/subdevice 1.
            put(c, "POWER",28,"MUTE",21,"CH_UP",27,"CH_DOWN",26,"VOL_UP",31,"VOL_DOWN",30,
                    "MENU",16,"INPUT",13,"BACK",14,"INFO",22,
                    "0",0,"1",1,"2",2,"3",3,"4",4,"5",5,"6",6,"7",7,"8",8,"9",9);
        }
        return Collections.unmodifiableMap(c);
    }

    static int power(String profile) {
        Integer value = commands(profile).get("POWER");
        if (value == null) throw new IllegalArgumentException("Profile has no power command");
        return value;
    }

    static String title(String profile) {
        if (PROFILE_STAOS.equals(profile)) return "Konka STAOS • اتجاهات وOK";
        return PROFILE_5040_B.equals(profile) ? "Konka KK-Y199" : "HiVER H43F01 • RID 5040-A";
    }

    private static void put(Map<String, Integer> map, Object... pairs) {
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], (Integer) pairs[i + 1]);
    }

    private RemoteProfiles() {}
}
