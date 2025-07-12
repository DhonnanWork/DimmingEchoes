package com.dimmingechoes.save;

import java.util.HashSet;
import java.util.Set;

public class SaveData {
    public int crystalCount;
    public Set<String> crystalRecipients = new HashSet<>();
    public long saveTimestamp;
    
    public SaveData() {
        this.saveTimestamp = System.currentTimeMillis();
    }
    
    public String getSaveTimeString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(saveTimestamp));
    }
}
