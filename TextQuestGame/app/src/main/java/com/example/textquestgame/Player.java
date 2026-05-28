package com.example.textquestgame;

public class Player {
    private String name;
    private int health;
    private int energy;

    public Player(String name) {
        this.name = name;
        this.health = 100;
        this.energy = 100;
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        health = Math.min(100, health + amount);
    }

    public void useEnergy(int amount) {
        energy = Math.max(0, energy - amount);
    }

    public void addEnergy(int amount) {
        energy += amount;
    }

    public String getName() { return name; }
    public int getEnergy() { return energy; }
    public int getHealth() { return health; }
}