package com.dimmingEchoes.logic;

import java.util.ArrayList;
import java.util.List;

public class UsageLog {

    private final List<String> log = new ArrayList<>();

    public void logCrystalUse(String npcName) {
        log.add(npcName);
        System.out.println("Crystal used for " + npcName);
    }

    public List<String> getLog() {
        return log;
    }

    public int getUsedCount() {
        return log.size();
    }
}
