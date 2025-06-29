package com.dimmingEchoes.entities;

import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.dialogue.DialogueNode;
import com.dimmingEchoes.dialogue.DialogueChoice;
import com.dimmingEchoes.dungeon.RoomType;

public class NPC {
    private final String name;
    private final int x, y;
    private boolean receivedCrystal = false;
    private DialogueNode rootDialogue;

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

    public DialogueNode getRootDialogue() {
        return rootDialogue;
    }

    public String giveCrystal(TheDimmingEcho game) {
        if (receivedCrystal) return name + " has already received a crystal.";
        if (game.getCrystalInventory().getCrystals() > 0) {
            game.getCrystalInventory().useCrystal();
            receivedCrystal = true;
            return "You gave a crystal to " + name + ". A memory stirs...";
        } else {
            return "You have no more crystals.";
        }
    }

    private DialogueNode buildDialogue(String name, RoomType roomType) {
        switch (name) {
            case "The Keeper" -> {
                DialogueNode end = new DialogueNode("You have the answers. You're just afraid of them.", null, false, false, true);
                DialogueNode branch1 = new DialogueNode(
                    "But... you always return.",
                    new DialogueChoice[]{
                        new DialogueChoice("What do you want from me?", end),
                        new DialogueChoice("I want answers.", end)
                    }, false, false, false);
                return new DialogueNode(
                    "You shouldn't be here.",
                    new DialogueChoice[]{
                        new DialogueChoice("I don't remember coming here.", branch1),
                        new DialogueChoice("Then let me leave.", new DialogueNode(
                            "Not yet. Not until you finish what you started.", null, false, false, true))
                    }, false, false, false);
            }
            case "The Whisper" -> {
                DialogueNode shared = new DialogueNode("Pain is the soil of memory.", null, false, false, true);
                DialogueNode branch1 = new DialogueNode(
                    "They're asking why you buried the joy.",
                    new DialogueChoice[]{
                        new DialogueChoice("It hurt too much.", shared),
                        new DialogueChoice("I needed to survive.", shared)
                    }, false, false, false);
                return new DialogueNode(
                    "Echoes remain, even after silence.",
                    new DialogueChoice[]{
                        new DialogueChoice("What are they saying?", branch1),
                        new DialogueChoice("I try not to listen.", new DialogueNode(
                            "Then they'll grow louder.", null, false, false, true))
                    }, false, false, false);
            }
            case "The Laughing Girl" -> {
                DialogueNode gaveCrystal = new DialogueNode("Thank you. I remember now...", null, true, true, true);
                DialogueNode rejectMemory = new DialogueNode("Then I’ll wait. Like before.", null, false, false, true);
                DialogueNode memoryOffer = new DialogueNode(
                    "Do you want me to remember it with you?",
                    new DialogueChoice[]{
                        new DialogueChoice("Yes.", gaveCrystal),
                        new DialogueChoice("No. Some memories are better left behind.", rejectMemory)
                    }, false, false, false);
                return new DialogueNode(
                    "You remembered something happy.",
                    new DialogueChoice[]{
                        new DialogueChoice("Was that you?", memoryOffer),
                        new DialogueChoice("It felt... warm.", memoryOffer)
                    }, false, false, false);
            }
            case "The Stranger" -> {
                DialogueNode acceptCrystal = new DialogueNode("You think a gift will save you? ...But I accept it.", null, true, true, true);
                DialogueNode prepareBattle = new DialogueNode("Then prove it.", null, false, false, true);
                DialogueNode readyToBreak = new DialogueNode(
                    "Finally. Are you ready to break the cycle?",
                    new DialogueChoice[]{
                        new DialogueChoice("I’m not afraid.", prepareBattle),
                        new DialogueChoice("I brought something for you.", acceptCrystal)
                    }, false, false, false);
                return new DialogueNode(
                    "You again?",
                    new DialogueChoice[]{
                        new DialogueChoice("I came to face you.", readyToBreak),
                        new DialogueChoice("I don't know why I'm here.", new DialogueNode(
                            "Then listen to your echo. It brought you here.", null, false, false, true))
                    }, false, false, false);
            }
            default -> {
                return new DialogueNode("...", null, false, false, true);
            }
        }
    }
}
