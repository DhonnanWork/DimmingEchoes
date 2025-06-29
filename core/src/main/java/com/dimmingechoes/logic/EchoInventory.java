package com.dimmingEchoes.logic;

public class EchoInventory {
    private int crystals;

    public EchoInventory(int initialCrystals) {
        this.crystals = initialCrystals;
    }

    public boolean useCrystal() {
        if (crystals > 0) {
            crystals--;
            return true;
        }
        return false;
    }

    public int getCrystals() {
        return crystals;
    }
}
