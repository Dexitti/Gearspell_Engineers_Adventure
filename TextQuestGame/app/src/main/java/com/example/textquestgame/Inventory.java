package com.example.textquestgame;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<InventoryItem> items = new ArrayList<>();
    private static final int MAX_SIZE = 5;

    public boolean addItem(String name, String description) {
        if (items.size() >= MAX_SIZE) return false;
        items.add(new InventoryItem(name, description));
        return true;
    }

    public boolean hasItem(String itemName) {
        for (InventoryItem item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }

    public void removeItem(String itemName) {
        items.removeIf(item -> item.getName().equalsIgnoreCase(itemName));
    }

    public List<InventoryItem> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }
    public int getSize() { return items.size(); }
}