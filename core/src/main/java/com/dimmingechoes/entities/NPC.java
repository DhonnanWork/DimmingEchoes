// File: core/src/main/java/com/dimmingechoes/entities/NPC.java

package com.dimmingechoes.entities;

import com.badlogic.gdx.math.Rectangle;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.dialogue.DialogueChoice;
import com.dimmingechoes.dialogue.DialogueNode;
import com.dimmingechoes.dungeon.RoomType;

import java.util.ArrayList;
import java.util.List;

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

        // The Laughing Girl special puzzle logic
        if (name.equals("The Laughing Girl")) {
            boolean allPuzzlesSolved = game.isPuzzleWordleSolved() && game.isPuzzleWordLadderSolved() && game.isPuzzleFibonacciSolved();
            if (allPuzzlesSolved) {
                DialogueNode end = new DialogueNode("...", new DialogueChoice[]{}, false, false, true);
                DialogueNode giveCrystal = new DialogueNode("You offer the crystal...", new DialogueChoice[]{ new DialogueChoice("...", end) }, true, true, false);
                return new DialogueNode("You solved them all! I... I remember so much more now. Thank you!",
                    new DialogueChoice[]{
                        new DialogueChoice("[Give a Crystal] Help her remember more.", giveCrystal),
                        new DialogueChoice("You're welcome.", end)
                    }, false, false, false);
            }
            return getPuzzleSelectionDialogue(game);
        }

        if (!hasHadInitialConversation) {
            hasHadInitialConversation = true;
            return getInitialDialogue(game);
        }

        return getLoopingDialogue(game);
    }

    private DialogueNode getPuzzleSelectionDialogue(TheDimmingEcho game) {
        List<DialogueChoice> puzzleChoices = new ArrayList<>();
        if (!game.isPuzzleWordleSolved()) {
            puzzleChoices.add(new DialogueChoice("[Attempt the word puzzle]", null));
        }
        if (!game.isPuzzleWordLadderSolved()) {
            puzzleChoices.add(new DialogueChoice("[Attempt the ladder puzzle]", null));
        }
        if (!game.isPuzzleFibonacciSolved()) {
            puzzleChoices.add(new DialogueChoice("[Attempt the number puzzle]", null));
        }
        puzzleChoices.add(new DialogueChoice("I'll come back later.", new DialogueNode("...", new DialogueChoice[]{}, false, false, true)));

        return new DialogueNode("Hee hee... I remember bits and pieces, like games! Which one do you want to play?",
            puzzleChoices.toArray(new DialogueChoice[0]), false, false, false);
    }

    private DialogueNode getInitialDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[]{}, false, false, true);

        switch (name) {
            case "The Stranger": {
                DialogueNode askAngry = new DialogueNode("You weren't there. You didn't see. Some things are better left buried.", new DialogueChoice[]{new DialogueChoice("I will find out.", end)}, false, false, false);
                return new DialogueNode("You again. What do you want?",
                    new DialogueChoice[]{
                        new DialogueChoice("Why are you so angry?", askAngry),
                        new DialogueChoice("Leave.", end)
                    }, false, false, false);
            }
        }
        return end;
    }

    private DialogueNode getLoopingDialogue(TheDimmingEcho game) {
        DialogueNode end = new DialogueNode("...", new DialogueChoice[]{}, false, false, true);

        switch (name) {
            case "The Stranger":
                 return new DialogueNode("Still here? State your business or leave.",
                    new DialogueChoice[]{
                        new DialogueChoice("Leaving.", end)
                    }, false, false, false);
        }
        return end;
    }

    private DialogueNode getFinalDialogue(TheDimmingEcho game) {
        switch (name) {
            case "The Laughing Girl":
                return new DialogueNode(
                    "...we were in a garden. You promised we'd come back. Was it sunny that day? I think it was.",
                    new DialogueChoice[]{}, false, false, true);
            case "The Stranger":
                return new DialogueNode(
                    "It was my fault. I... I couldn't protect her. The laughter stopped because of me. But now, I feel... at peace.",
                    new DialogueChoice[]{}, false, false, true);
        }
        return new DialogueNode("...", new DialogueChoice[]{}, false, false, true);
    }
}
