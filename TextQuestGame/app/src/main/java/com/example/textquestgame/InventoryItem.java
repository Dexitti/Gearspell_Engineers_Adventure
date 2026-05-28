package com.example.textquestgame;

public class InventoryItem {
    private String name;
    private String description;

    public InventoryItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}