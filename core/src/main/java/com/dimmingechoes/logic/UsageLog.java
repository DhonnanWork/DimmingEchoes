package com.dimmingEchoes;

import java.util.HashSet;
import java.util.Set;

public class UsageLog {
    private final Set<String> npcsGivenCrystal = new HashSet<>();

    public void logCrystalGiven(String npcName) {
        npcsGivenCrystal.add(npcName);
    }

    public boolean hasGivenCrystal(String npcName) {
        return npcsGivenCrystal.contains(npcName);
    }

    public int totalGiven() {
        return npcsGivenCrystal.size();
    }

    public Set<String> getAllRecipients() {
        return new HashSet<>(npcsGivenCrystal);
    }

    public void clear() {
        npcsGivenCrystal.clear();
    }
}
