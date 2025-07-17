package com.dimmingechoes;

import com.badlogic.gdx.Game;
import com.dimmingechoes.screens.PrologueScreen;
import com.dimmingechoes.screens.DungeonScreen;
import com.dimmingechoes.save.SaveData;
import com.dimmingechoes.save.SaveManager;
import com.dimmingechoes.logic.CrystalInventory;
import com.dimmingechoes.logic.UsageLog;
import com.dimmingechoes.screens.MainMenuScreen;
import com.badlogic.gdx.math.Vector2;

public class TheDimmingEcho extends Game {

    private CrystalInventory crystalInventory;
    private UsageLog usageLog;
    private int crystalsLostToFailure = 0;
    private boolean puzzleWordleSolved = false;
    private boolean puzzleWordLadderSolved = false;
    private boolean puzzleFibonacciSolved = false;
    private Vector2 nextPlayerPosition = null;
    private String nextRoomTmxPath = null;

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

    public boolean isPuzzleWordleSolved() { return puzzleWordleSolved; }
    public void setPuzzleWordleSolved(boolean puzzleWordleSolved) { this.puzzleWordleSolved = puzzleWordleSolved; }
    public boolean isPuzzleWordLadderSolved() { return puzzleWordLadderSolved; }
    public void setPuzzleWordLadderSolved(boolean puzzleWordLadderSolved) { this.puzzleWordLadderSolved = puzzleWordLadderSolved; }
    public boolean isPuzzleFibonacciSolved() { return puzzleFibonacciSolved; }
    public void setPuzzleFibonacciSolved(boolean puzzleFibonacciSolved) { this.puzzleFibonacciSolved = puzzleFibonacciSolved; }

    public Vector2 getNextPlayerPosition() {
        return nextPlayerPosition;
    }
    public String getNextRoomTmxPath() {
        return nextRoomTmxPath;
    }
    public void clearNextSpawnPoint() {
        this.nextPlayerPosition = null;
        this.nextRoomTmxPath = null;
    }

    // Change saveGame to accept DungeonScreen
    public void saveGame(int slot, DungeonScreen dungeonScreen) {
        SaveData data = new SaveData();
        data.crystalCount = crystalInventory.getCrystals();
        data.crystalRecipients = usageLog.getAllRecipients();
        data.puzzleWordleSolved = this.puzzleWordleSolved;
        data.puzzleWordLadderSolved = this.puzzleWordLadderSolved;
        data.puzzleFibonacciSolved = this.puzzleFibonacciSolved;
        data.playerX = dungeonScreen.getPlayer().x;
        data.playerY = dungeonScreen.getPlayer().y;
        data.currentRoomTmxPath = dungeonScreen.getCurrentRoom().getTmxPath();
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
            this.puzzleWordleSolved = data.puzzleWordleSolved;
            this.puzzleWordLadderSolved = data.puzzleWordLadderSolved;
            this.puzzleFibonacciSolved = data.puzzleFibonacciSolved;
            this.nextPlayerPosition = new Vector2(data.playerX, data.playerY);
            this.nextRoomTmxPath = data.currentRoomTmxPath;
        }
    }

    // Legacy method for backward compatibility
    public void saveGame() {
        saveGame(1, (DungeonScreen) getScreen());
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
        puzzleWordleSolved = false;
        puzzleWordLadderSolved = false;
        puzzleFibonacciSolved = false;
        clearNextSpawnPoint();
    }
}
