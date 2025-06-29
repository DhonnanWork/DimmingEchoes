package com.dimmingEchoes;

import com.badlogic.gdx.Game;
import com.dimmingEchoes.screens.DungeonScreen;
import com.dimmingEchoes.logic.EchoInventory;
import com.dimmingEchoes.logic.UsageLog;

/**
 * Main game class for The Dimming Echo.
 * Handles screen transitions and global game state (like crystal inventory and usage log).
 */
public class TheDimmingEcho extends Game {

    private EchoInventory crystalInventory;
    private UsageLog usageLog;

    @Override
    public void create() {
        // Initialize core systems
        crystalInventory = new EchoInventory(5);  // Starts with 5 crystals
        usageLog = new UsageLog();

        // Start on the dungeon screen (starting room)
        this.setScreen(new DungeonScreen(this));
    }

    public EchoInventory getCrystalInventory() {
        return crystalInventory;
    }

    public UsageLog getUsageLog() {
        return usageLog;
    }
}
