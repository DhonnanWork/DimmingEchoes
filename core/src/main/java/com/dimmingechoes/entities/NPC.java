// File: core/src/main/java/com/dimmingechoes/entities/NPC.java

package com.dimmingechoes.entities;

import com.badlogic.gdx.math.Rectangle;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.dialogue.DialogueChoice;
import com.dimmingechoes.dialogue.DialogueNode;
import com.dimmingechoes.dungeon.RoomType;

public class NPC {
    private final String name;
    private final float x, y;
    private final Rectangle bounds;
    public final int gid;
    private boolean hasHadInitialConversation = false;
    private static final float NPC_INTERACTION_WIDTH = 48f;
    private static final float NPC_INTERACTION_HEIGHT = 48f;

    public NPC(String name, RoomType room, float x, float y, int gid) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.gid = gid;
        this.bounds = new Rectangle(x - NPC_INTERACTION_WIDTH / 2, y, NPC_INTERACTION_WIDTH, NPC_INTERACTION_HEIGHT);
    }

    public String getName() { return name; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Rectangle getBounds() { return bounds; }

    public boolean hasReceivedCrystal(TheDimmingEcho game) {
        return game.getUsageLog().hasGivenCrystal(this.name);
    }

    public DialogueNode getDialogue(TheDimmingEcho game) {
        if (hasReceivedCrystal(game)) {
            return getFinalDialogue(game);
        }

        if (name.equals("The Laughing Girl")) {
            try {
                com.dimmingechoes.screens.DungeonScreen ds = (com.dimmingechoes.screens.DungeonScreen) game.getScreen();
                if (ds != null && ds.isMemoryPuzzleSolved()) {
                    return new DialogueNode("You solved it! It was a book! I remember now... thank you!", new DialogueChoice[0], false, false, true);
                }
            } catch (ClassCastException e) {
                // Ignore if not on DungeonScreen
            }
        }

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

        if (!hasHadInitialConversation) {
            hasHadInitialConversation = true;
            return getInitialDialogue(game);
        }

        return getLoopingDialogue(game);
    }

    private DialogueNode getInitialDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[0], false, false, true);
        DialogueNode giveCrystal = new DialogueNode("You offer the crystal...", new DialogueChoice[]{ new DialogueChoice("...", end) }, true, true, false);

        switch (name) {
            case "The Laughing One":
                DialogueNode askAboutPlace = new DialogueNode("This place is a reflection of what was lost. To remember, you must give a part of yourself.", new DialogueChoice[]{new DialogueChoice("I see.", end)}, false, false, false);
                return new DialogueNode("You have returned to the place of echoes. What will you do?",
                    new DialogueChoice[]{
                        new DialogueChoice("Ask about this place.", askAboutPlace),
                        new DialogueChoice("[Give a Crystal] Restore an echo.", giveCrystal),
                        new DialogueChoice("Say nothing.", end) }, false, false, false);
            case "The Laughing Girl":
                return new DialogueNode(
                    "Hee hee... I remember... something. It has leaves, but it's not a tree. It has a spine, but no bones... Find it for me? Please?",
                    new DialogueChoice[]{ new DialogueChoice("I will look for it.", end) },
                    false, false, false
                );
            case "The Stranger":
                DialogueNode askAngry = new DialogueNode("You weren't there. You didn't see. Some things are better left buried.", new DialogueChoice[]{new DialogueChoice("I will find out.", end)}, false, false, false);
                return new DialogueNode("You again. What do you want?",
                    new DialogueChoice[]{
                        new DialogueChoice("Why are you so angry?", askAngry),
                        new DialogueChoice("[Give a Crystal] Quell his anger.", giveCrystal),
                        new DialogueChoice("Fight", new DialogueNode("You want to fight? Fine. Let's settle this.", new DialogueChoice[]{ new DialogueChoice("Begin Battle", null) }, false, false, false)),
                        new DialogueChoice("Leave.", end) }, false, false, false);
            case "The Whisper":
                return new DialogueNode("...hush... not yet...", new DialogueChoice[0], false, false, true);
        }
        return end;
    }

    private DialogueNode getLoopingDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[0], false, false, true);
        DialogueNode giveCrystal = new DialogueNode("You offer the crystal...", new DialogueChoice[]{ new DialogueChoice("...", end) }, true, true, false);

        switch (name) {
            case "The Laughing One":
                return new DialogueNode("Still you linger in the dust of what was.", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Leave.", end) }, false, false, false);
            case "The Laughing Girl":
                return new DialogueNode("Hee hee... still can't remember? Find the thing with leaves but no tree...", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Not yet.", end) }, false, false, false);
            case "The Stranger":
                DialogueNode fightNode2 = new DialogueNode("You want to fight? Fine. Let's settle this.", new DialogueChoice[]{ new DialogueChoice("Begin Battle", null) }, false, false, false);
                return new DialogueNode("Still here? State your business or leave.", new DialogueChoice[]{ new DialogueChoice("[Give Crystal]", giveCrystal), new DialogueChoice("Fight", fightNode2), new DialogueChoice("Leaving.", end) }, false, false, false);
            case "The Whisper":
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

    private DialogueNode getFinalDialogue(TheDimmingEcho game) {
        switch (name) {
            case "The Laughing One":
                return new DialogueNode("The echo you restored in me is quiet. It is at peace. Thank you.", new DialogueChoice[0], false, false, true);
            case "The Laughing Girl":
                return new DialogueNode("...we were in a garden. You promised we'd come back. Was it sunny that day? I think it was.", new DialogueChoice[0], false, false, true);
            case "The Stranger":
                return new DialogueNode("It was my fault. I... I couldn't protect her. The laughter stopped because of me. But now, I feel... at peace.", new DialogueChoice[0], false, false, true);
            case "The Whisper":
                return new DialogueNode("This was never the memory of a place. It is the memory of a person. You. This is your own shattered heart, struggling to mend.", new DialogueChoice[0], false, false, true);
        }
        return new DialogueNode("...", new DialogueChoice[0], false, false, true);
    }
}
