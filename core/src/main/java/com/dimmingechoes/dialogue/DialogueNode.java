package com.dimmingEchoes.dialogue;

public class DialogueNode {
    public final String text;
    public final DialogueChoice[] choices;
    public final boolean requiresCrystal;
    public final boolean consumesCrystal;
    public final boolean endDialogue;

    public DialogueNode(String text, DialogueChoice[] choices, boolean requiresCrystal, boolean consumesCrystal, boolean endDialogue) {
        this.text = text;
        this.choices = choices;
        this.requiresCrystal = requiresCrystal;
        this.consumesCrystal = consumesCrystal;
        this.endDialogue = endDialogue;
    }
}
