package com.dimmingEchoes.entities;

import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.dialogue.DialogueChoice;
import com.dimmingEchoes.dialogue.DialogueNode;
import com.dimmingEchoes.dungeon.RoomType;

public class NPC {
    private final String name;
    private final int x, y;
    private boolean receivedCrystal = false;
    private final DialogueNode rootDialogue;
    private DialogueNode lastDialogueNode = null;

    public NPC(String name, RoomType room, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.rootDialogue = buildDialogue(name, room);
    }

    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }

    public boolean hasReceivedCrystal() { return receivedCrystal; }
    public DialogueNode getRootDialogue() { return rootDialogue; }

    public DialogueNode getLastDialogue() {
        return lastDialogueNode;
    }

    public void setLastDialogue(DialogueNode node) {
        lastDialogueNode = node;
    }

    public String giveCrystal(TheDimmingEcho game) {
        if (receivedCrystal) return name + " has already received a crystal.";
        if (game.getCrystalInventory().getCrystals() > 0) {
            game.getCrystalInventory().useCrystal();
            receivedCrystal = true;
            game.getUsageLog().logCrystalGiven(this.name);
            return "You gave a crystal to " + name + ". A memory stirs...";
        } else {
            return "You have no more crystals.";
        }
    }

    private DialogueNode buildDialogue(String name, RoomType roomType) {
        // Provide full branching trees per NPC based on previous implementation
        return new DialogueNode("...", null, false, false, true);
    }
}
