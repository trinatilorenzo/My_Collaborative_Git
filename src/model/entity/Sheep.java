package model.entity;

import java.awt.Rectangle;
import model.object.Item;

import main.GameSetting;


public class Sheep extends Entity {

    private int health = 2;
    private boolean dead = false;

    public Sheep(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = 1; // Example speed value
        this.direction = GameSetting.Direction.DOWN; // Default direction
        this.currentLayer = 0; // Default layer

        // Initialize hitbox (example values, adjust as needed)
        this.solidArea = new Rectangle(0, 0, 32, 32); // Assuming a 32x32 sprite
    }

    public Item hit(){
        if (!dead) {
            health--;
            if (health <= 0) {
                dead = true;
                // Optionally, you can set solid to false or perform other logic when the sheep dies
                return new Item("MEAT", worldX, worldY, 1); // Example item drop
            }
        }
        return null;
    }
    
}
