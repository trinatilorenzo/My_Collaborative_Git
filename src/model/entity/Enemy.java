package model.entity;

import java.awt.Rectangle;

public abstract class Enemy extends Entity {

    protected int maxHealth;
    protected int currentHealth;
    protected boolean alive = true;

    public Enemy(int worldX, int worldY, int health) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.maxHealth = health;
        this.currentHealth = health;

        solidArea = new Rectangle(0, 0, 32, 32); // esempio
    }

    public void takeDamage(int damage) {
        if (!alive) return;

        currentHealth -= damage;

        if (currentHealth <= 0) {
            die();
        }
    }

    protected void die() {
        alive = false;
        System.out.println("Enemy morto");
    }

    public boolean isAlive() {
        return alive;
    }

    public Rectangle getSolidWorldArea() {
        return new Rectangle(
            worldX + solidArea.x,
            worldY + solidArea.y,
            solidArea.width,
            solidArea.height
        );
    }

    // ogni nemico può avere comportamento diverso
    public abstract void update(double deltaMs);
}