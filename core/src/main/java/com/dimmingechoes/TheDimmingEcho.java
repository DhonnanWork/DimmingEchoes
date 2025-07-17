package com.dimmingechoes;

import com.badlogic.gdx.Game;
import com.dimmingechoes.screens.PrologueScreen;
import com.dimmingechoes.screens.DungeonScreen;
import com.dimmingechoes.save.SaveData;
import com.dimmingechoes.save.SaveManager;
import com.dimmingechoes.logic.CrystalInventory;
import com.dimmingechoes.logic.UsageLog;
import com.dimmingechoes.screens.MainMenuScreen;

public class TheDimmingEcho extends Game {

    private CrystalInventory crystalInventory;
    private UsageLog usageLog;
    private int crystalsLostToFailure = 0;

    @Override
    public void create() {
        crystalInventory = new CrystalInventory(5);
        usageLog = new UsageLog();

        loadGame(1); // Default to slot 1 on startup
        setScreen(new MainMenuScreen(this));
    }

    public CrystalInventory getCrystalInventory() {
        return crystalInventory;
    }

    public UsageLog getUsageLog() {
        return usageLog;
    }

    public void saveGame(int slot) {
        SaveData data = new SaveData();
        data.crystalCount = crystalInventory.getCrystals();
        data.crystalRecipients = usageLog.getAllRecipients();
        SaveManager.save(data, slot);
    }

    public void loadGame(int slot) {
        SaveData data = SaveManager.load(slot);
        if (data != null) {
            crystalInventory.reset(data.crystalCount);
            usageLog.clear(); // Clear existing data
            for (String npc : data.crystalRecipients) {
                usageLog.logCrystalGiven(npc);
            }
        }
    }

    // Legacy method for backward compatibility
    public void saveGame() {
        saveGame(1);
    }

    // Legacy method for backward compatibility
    public void loadGame() {
        loadGame(1);
    }

    // Add failure tracker methods
    public void logFailure() {
        crystalsLostToFailure++;
    }
    public int getCrystalsLostToFailure() {
        return crystalsLostToFailure;
    }
    public void resetGame() {
        crystalInventory = new CrystalInventory(5);
        usageLog = new UsageLog();
        crystalsLostToFailure = 0;
    }
}
