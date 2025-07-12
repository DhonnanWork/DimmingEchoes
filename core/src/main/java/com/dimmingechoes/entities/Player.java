package com.dimmingechoes.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;

public class Player {
    private int health;
    private int maxHealth;
    private int attackPower;
    private int defense;
    private Animation<TextureRegion> animation;
    private String name;

    public Player(String name, int maxHealth, int attackPower, int defense, Animation<TextureRegion> animation) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attackPower = attackPower;
        this.defense = defense;
        this.animation = animation;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getDefense() {
        return defense;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }
}
