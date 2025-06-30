package com.dimmingEchoes;

public class CrystalInventory {
    private int crystals;

    public CrystalInventory(int startingAmount) {
        this.crystals = startingAmount;
    }

    public int getCrystals() {
        return crystals;
    }

    public boolean useCrystal() {
        if (crystals > 0) {
            crystals--;
            return true;
        }
        return false;
    }

    public void addCrystal() {
        crystals++;
    }

    public void reset(int value) {
        crystals = value;
    }
}
