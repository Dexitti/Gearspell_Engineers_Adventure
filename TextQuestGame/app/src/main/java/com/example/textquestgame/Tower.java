package com.example.textquestgame;

public class Tower {
    private int health;

    public Tower() {
        this.health = 60;
    }

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public void repair(int amount) {
        health = Math.min(200, health + amount);
    }

    public int getHealth() { return health; }
}