package com.yosef.konkaremote;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProfileIntegrityCheck {
    public static void main(String[] args) {
        verify(RemoteProfiles.PROFILE_5040_A,
                new String[]{"POWER=28","MUTE=21","CH_UP=27","CH_DOWN=26","VOL_UP=31","VOL_DOWN=30",
                        "MENU=16","INPUT=13","BACK=14","INFO=22",
                        "0=0","1=1","2=2","3=3","4=4","5=5","6=6","7=7","8=8","9=9"});
        verify(RemoteProfiles.PROFILE_5040_B,
                new String[]{"POWER=1","MUTE=10","CH_UP=2","CH_DOWN=3","VOL_UP=4","VOL_DOWN=5",
                        "MENU=29","INPUT=14","BACK=12","INFO=11","SLEEP=13",
                        "0=16","1=17","2=18","3=19","4=20","5=21","6=22","7=23","8=24","9=25"});
        System.out.println("Verified public Konka profiles: no unknown or guessed commands");
    }

    private static void verify(String profile, String[] expectedPairs) {
        Map<String, Integer> actual = RemoteProfiles.commands(profile);
        Set<String> expectedKeys = new HashSet<>();
        for (String pair : expectedPairs) {
            String[] parts = pair.split("=");
            expectedKeys.add(parts[0]);
            Integer value = actual.get(parts[0]);
            if (value == null || value != Integer.parseInt(parts[1])) {
                throw new AssertionError(profile + " mismatch: " + pair + ", actual=" + value);
            }
        }
        if (!actual.keySet().equals(expectedKeys)) {
            throw new AssertionError(profile + " contains unverified keys: " +
                    difference(actual.keySet(), expectedKeys));
        }
    }

    private static Set<String> difference(Set<String> actual, Set<String> expected) {
        Set<String> extra = new HashSet<>(actual);
        extra.removeAll(expected);
        return extra;
    }

    private ProfileIntegrityCheck() {}
}
