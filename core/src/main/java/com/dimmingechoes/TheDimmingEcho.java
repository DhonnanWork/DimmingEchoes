package com.dimmingechoes;

import com.badlogic.gdx.Game;
import com.dimmingechoes.screens.DungeonScreen;
import com.dimmingechoes.save.SaveData;
import com.dimmingechoes.save.SaveManager;
import com.dimmingechoes.logic.CrystalInventory;
import com.dimmingechoes.logic.UsageLog;

public class TheDimmingEcho extends Game {

    private CrystalInventory crystalInventory;
    private UsageLog usageLog;

    @Override
    public void create() {
        crystalInventory = new CrystalInventory(5);
        usageLog = new UsageLog();

        loadGame();
        setScreen(new DungeonScreen(this));
    }

    public CrystalInventory getCrystalInventory() {
        return crystalInventory;
    }

    public UsageLog getUsageLog() {
        return usageLog;
    }

    public void saveGame() {
        SaveData data = new SaveData();
        data.crystalCount = crystalInventory.getCrystals();
        data.crystalRecipients = usageLog.getAllRecipients();
        SaveManager.save(data);
    }

    public void loadGame() {
        SaveData data = SaveManager.load();
        if (data != null) {
            crystalInventory.reset(data.crystalCount);
            for (String npc : data.crystalRecipients) {
                usageLog.logCrystalGiven(npc);
            }
        }
    }
}
