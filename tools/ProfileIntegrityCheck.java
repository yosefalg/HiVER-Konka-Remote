package com.yosef.konkaremote;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProfileIntegrityCheck {
    public static void main(String[] args) {
        verify(RemoteProfiles.PROFILE_STAOS, new String[]{
                "POWER=11","MUTE=20","CH_UP=17","CH_DOWN=16","VOL_UP=19","VOL_DOWN=18",
                "UP=43","DOWN=44","LEFT=45","RIGHT=46","ENTER=47","MENU=21","INPUT=28","BACK=48","INFO=10","HOME=56",
                "0=0","1=1","2=2","3=3","4=4","5=5","6=6","7=7","8=8","9=9","RED=26","GREEN=31","YELLOW=12","BLUE=22"});
        verify(RemoteProfiles.PROFILE_CHANGHONG, new String[]{
                "POWER=18","MUTE=16","CH_UP=27","CH_DOWN=31","VOL_UP=26","VOL_DOWN=30",
                "UP=25","DOWN=29","LEFT=70","RIGHT=71","ENTER=10","MENU=91","INPUT=83","BACK=64","INFO=22","HOME=81",
                "0=0","1=1","2=2","3=3","4=4","5=5","6=6","7=7","8=8","9=9"});
        verify(RemoteProfiles.PROFILE_HISENSE, new String[]{
                "POWER=13","MUTE=14","CH_UP=74","CH_DOWN=75","VOL_UP=68","VOL_DOWN=67",
                "UP=22","DOWN=23","LEFT=25","RIGHT=24","ENTER=21","MENU=20","INPUT=18","BACK=72","INFO=149","HOME=148","SLEEP=26",
                "0=0","1=1","2=2","3=3","4=4","5=5","6=6","7=7","8=8","9=9"});
        verify(RemoteProfiles.PROFILE_MTK, new String[]{
                "POWER=70","MUTE=90","CH_UP=31","CH_DOWN=25","VOL_UP=22","VOL_DOWN=21",
                "UP=82","DOWN=19","LEFT=6","RIGHT=26","ENTER=15","MENU=7","INPUT=4","BACK=27","INFO=20","HOME=23","SLEEP=69",
                "0=80","1=73","2=85","3=89","4=77","5=81","6=93","7=72","8=84","9=88"});
        if (!RemoteProfiles.usesKonkaProtocol(RemoteProfiles.PROFILE_STAOS)) throw new AssertionError("Konka protocol routing");
        if (RemoteProfiles.necCustomerCode(RemoteProfiles.PROFILE_CHANGHONG) != 0x40BF
                || RemoteProfiles.necCustomerCode(RemoteProfiles.PROFILE_HISENSE) != 0x00BF
                || RemoteProfiles.necCustomerCode(RemoteProfiles.PROFILE_MTK) != 0x807F) throw new AssertionError("NEC customer codes");
        if (!RemoteProfiles.PROFILE_STAOS.equals(RemoteProfiles.normalize("rid5040_a"))
                || !RemoteProfiles.PROFILE_STAOS.equals(RemoteProfiles.normalize("rid5040_b"))) throw new AssertionError("legacy preference migration");
        if (RemoteProfiles.allProfiles().length != 4) throw new AssertionError("profile catalog size");
        for (String profile : RemoteProfiles.allProfiles()) {
            if (RemoteProfiles.title(profile).contains("RID 5040")) throw new AssertionError("unverified RID claim in " + profile);
            if (!RemoteProfiles.commands(profile).containsKey("POWER")) throw new AssertionError("missing power " + profile);
        }
        verifySignalPatterns();
        System.out.println("PASS: 4 public MediaTek IR profiles; mappings, customer codes, legacy migration, RID labeling and IR timing patterns verified.");
    }

    private static void verifySignalPatterns() {
        int[] nec = IrSignalPatterns.nec(0x40BF, 0x12);
        if (nec.length != 67 || nec[0] != 9000 || nec[1] != 4500 || nec[nec.length - 1] != 560)
            throw new AssertionError("NEC frame shape/header/trailer");
        if (nec[14] != 560 || nec[15] != 1690) throw new AssertionError("NEC address bit order");
        if (nec[18] != 560 || nec[19] != 1690) throw new AssertionError("NEC inverse-address bit order");
        int[] konka = IrSignalPatterns.konkaStaos(0x0B);
        if (konka.length != 35 || konka[0] != 3000 || konka[1] != 3000 || konka[konka.length - 1] != 4000)
            throw new AssertionError("Konka frame shape/header/trailer");
        if (konka[14] != 500 || konka[15] != 2500) throw new AssertionError("Konka bit order");
        for (int duration : nec) if (duration <= 0) throw new AssertionError("invalid NEC duration");
        for (int duration : konka) if (duration <= 0) throw new AssertionError("invalid Konka duration");
        boolean rejected = false;
        try { IrSignalPatterns.nec(0x140BF, 0x12); } catch (IllegalArgumentException expected) { rejected = true; }
        if (!rejected) throw new AssertionError("invalid NEC customer code accepted");
    }

    private static void verify(String profile, String[] expectedPairs) {
        Map<String, Integer> actual = RemoteProfiles.commands(profile);
        Set<String> expectedKeys = new HashSet<>();
        for (String pair : expectedPairs) {
            String[] parts = pair.split("=");
            expectedKeys.add(parts[0]);
            Integer value = actual.get(parts[0]);
            if (value == null || value != Integer.parseInt(parts[1])) throw new AssertionError(profile + " mismatch: " + pair + ", actual=" + value);
        }
        if (!actual.keySet().equals(expectedKeys)) {
            Set<String> extra = new HashSet<>(actual.keySet()); extra.removeAll(expectedKeys);
            throw new AssertionError(profile + " unexpected keys: " + extra);
        }
    }

    private ProfileIntegrityCheck() {}
}
