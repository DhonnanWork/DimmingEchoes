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

    // This flag tracks if the NPC has had their first, main conversation.
    private boolean hasHadInitialConversation = false;

    private static final float NPC_SIZE = 48f;

    public NPC(String name, RoomType room, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x - NPC_SIZE / 2, y - NPC_SIZE / 2, NPC_SIZE, NPC_SIZE);
    }

    public String getName() { return name; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Rectangle getBounds() { return bounds; }

    public boolean hasReceivedCrystal(TheDimmingEcho game) {
        return game.getUsageLog().hasGivenCrystal(this.name);
    }

    /**
     * This method is a state machine that returns a dialogue tree based on game state.
     */
    public DialogueNode getDialogue(TheDimmingEcho game) {
        // The most important state is whether they have received a crystal. This is their "final" state.
        if (hasReceivedCrystal(game)) {
            return getFinalDialogue(game);
        }

        // Check for special, one-time reactive dialogue before the initial conversation.
        if (!hasHadInitialConversation) {
            if (name.equals("The Laughing Girl") && game.getUsageLog().hasGivenCrystal("The Stranger")) {
                hasHadInitialConversation = true;
                return new DialogueNode("That angry man... he seems less stormy now. Is he... sad?", new DialogueChoice[0], false, false, true);
            }
            if (name.equals("The Stranger") && game.getUsageLog().hasGivenCrystal("The Laughing Girl")) {
                hasHadInitialConversation = true;
                return new DialogueNode("I hear laughter on the wind. It's... faint. But it's there. What have you done?", new DialogueChoice[0], false, false, true);
            }
        }

        // If it's the first time talking to them, give the full initial dialogue.
        if (!hasHadInitialConversation) {
            hasHadInitialConversation = true; // Set the flag so this dialogue only appears once.
            return getInitialDialogue(game);
        }

        // If we reach here, it means we've talked before but haven't given a crystal. Give the looping dialogue.
        return getLoopingDialogue(game);
    }

    /**
     * The dialogue for the first time you speak to an NPC.
     */
    private DialogueNode getInitialDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[0], false, false, true);
        DialogueNode giveCrystal = new DialogueNode("You offer the crystal...", new DialogueChoice[]{ new DialogueChoice("...", end) }, true, true, false);

        switch (name) {
            case "The Keeper":
                DialogueNode askAboutPlace = new DialogueNode("This place is a reflection of what was lost. To remember, you must give a part of yourself.", new DialogueChoice[]{new DialogueChoice("I see.", end)}, false, false, false);
                return new DialogueNode("You have returned to the place of echoes. What will you do?",
                    new DialogueChoice[]{
                        new DialogueChoice("Ask about this place.", askAboutPlace),
                        new DialogueChoice("[Give a Crystal] Restore an echo.", giveCrystal),
                        new DialogueChoice("Say nothing.", end) }, false, false, false);
            case "The Laughing Girl":
                DialogueNode askForget = new DialogueNode("I don't know... It felt warm. Like the sun on my face. But now it's cold.", new DialogueChoice[]{new DialogueChoice("I'm sorry.", end)}, false, false, false);
                return new DialogueNode("Hee hee... did you forget something?",
                    new DialogueChoice[]{
                        new DialogueChoice("Forget what?", askForget),
                        new DialogueChoice("[Give a Crystal] Help her remember.", giveCrystal),
                        new DialogueChoice("Leave her be.", end) }, false, false, false);
            case "The Stranger":
                DialogueNode askAngry = new DialogueNode("You weren't there. You didn't see. Some things are better left buried.", new DialogueChoice[]{new DialogueChoice("I will find out.", end)}, false, false, false);
                return new DialogueNode("You again. What do you want?",
                    new DialogueChoice[]{
                        new DialogueChoice("Why are you so angry?", askAngry),
                        new DialogueChoice("[Give a Crystal] Quell his anger.", giveCrystal),
                        new DialogueChoice("Leave.", end) }, false, false, false);
            case "The Whisper":
                return new DialogueNode("...hush... not yet...", new DialogueChoice[0], false, false, true);
        }
        return end; // Default case
    }

    /**
     * The shorter dialogue for subsequent conversations before giving a crystal.
     */
    private DialogueNode getLoopingDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[0], false, false, true);
        DialogueNode giveCrystal = new DialogueNode("You offer the crystal...", new DialogueChoice[]{ new DialogueChoice("...", end) }, true, true, false);

        switch (name) {
            case "The Keeper":
                return new DialogueNode("Still you linger in the dust of what was.", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Leave.", end) }, false, false, false);
            case "The Laughing Girl":
                return new DialogueNode("Hee hee... still can't remember?", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Not yet.", end) }, false, false, false);
            case "The Stranger":
                return new DialogueNode("Still here? State your business or leave.", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Leaving.", end) }, false, false, false);
            case "The Whisper":
                // --- FIX: Use the 'game' parameter passed into this method ---
                boolean girlHelped = game.getUsageLog().hasGivenCrystal("The Laughing Girl");
                boolean strangerHelped = game.getUsageLog().hasGivenCrystal("The Stranger");
                if (girlHelped && strangerHelped) {
                    return new DialogueNode("You have pieced together the joy and the sorrow... Are you ready for the truth?",
                        new DialogueChoice[]{ new DialogueChoice("[Give the Final Crystal] Learn the truth.", giveCrystal), new DialogueChoice("I'm not ready.", end) }, false, false, false);
                }
                return new DialogueNode("...the echoes are not yet settled...", new DialogueChoice[0], false, false, true);
        }
        return end;
    }

    /**
     * The final, permanent dialogue after a crystal has been given.
     */
    private DialogueNode getFinalDialogue(TheDimmingEcho game) {
        switch (name) {
            case "The Keeper":
                return new DialogueNode("The echo you restored in me is quiet. It is at peace. Thank you.", new DialogueChoice[0], false, false, true);
            case "The Laughing Girl":
                return new DialogueNode("...we were in a garden. You promised we'd come back. Was it sunny that day? I think it was.", new DialogueChoice[0], false, false, true);
            case "The Stranger":
                return new DialogueNode("It was my fault. I... I couldn't protect her. The laughter stopped because of me.", new DialogueChoice[0], false, false, true);
            case "The Whisper":
                return new DialogueNode("This was never the memory of a place. It is the memory of a person. You. This is your own shattered heart, struggling to mend.", new DialogueChoice[0], false, false, true);
        }
        return new DialogueNode("...", new DialogueChoice[0], false, false, true);
    }
}
