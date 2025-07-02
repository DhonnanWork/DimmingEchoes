package com.dimmingEchoes.entities;

import com.badlogic.gdx.math.Rectangle;
import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.dialogue.DialogueChoice;
import com.dimmingEchoes.dialogue.DialogueNode;
import com.dimmingEchoes.dungeon.RoomType;

public class NPC {
    private final String name;
    private final float x, y;
    private final Rectangle bounds;
    private boolean receivedCrystal = false;
    private final DialogueNode rootDialogue;
    private DialogueNode lastDialogueNode = null;

    private static final float NPC_SIZE = 48f;

    public NPC(String name, RoomType room, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x - NPC_SIZE / 2, y - NPC_SIZE / 2, NPC_SIZE, NPC_SIZE);
        this.rootDialogue = buildDialogue(name, room);
    }

    public String getName() { return name; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Rectangle getBounds() { return bounds; }

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
        // TODO: This is where you'll build the actual branching dialogue trees.
        // MODIFICATION: Return an empty array instead of null for choices.
        return new DialogueNode("...", new DialogueChoice[0], false, false, true);
    }
}
