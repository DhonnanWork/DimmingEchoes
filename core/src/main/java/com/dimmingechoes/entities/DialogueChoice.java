package com.dimmingEchoes.entities;

public class DialogueChoice {
    public final String choiceText;
    public final DialogueNode next;

    public DialogueChoice(String choiceText, DialogueNode next) {
        this.choiceText = choiceText;
        this.next = next;
    }
}

