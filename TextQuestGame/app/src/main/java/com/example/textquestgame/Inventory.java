package com.example.textquestgame;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<String> items = new ArrayList<>();
    private static final int MAX_SIZE = 5;

    public boolean addItem(String name) {
        if (items.size() >= MAX_SIZE) return false;
        if (items.contains(name)) return true;
        items.add(name);
        return true;
    }

    public boolean hasItem(String itemName) {
        return items.contains(itemName);
    }

    public void removeItem(String itemName) {
        items.remove(itemName);
    }

    public List<String> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }
    public int getSize() { return items.size(); }
}