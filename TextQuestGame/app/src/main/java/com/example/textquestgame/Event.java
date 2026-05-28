package com.example.textquestgame;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String text;
    private List<Choice> choices;

    public Event(String text) {
        this.text = text;
        this.choices = new ArrayList<>();
    }
    public String getText() { return text; }

    public void addChoice(String choiceText) {
        choices.add(new Choice(choiceText));
    }

    public List<Choice> getChoices() { return choices; }
}