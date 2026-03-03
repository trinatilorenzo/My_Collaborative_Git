package model.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;


public class Entity {

    //Position and movement
    protected int worldX, worldY;
    protected int speed;
    protected Direction direction;

    // Hitbox and collision
    protected Rectangle solidArea;
    public boolean collisionOn = false;
    
    // Layering
    protected int currentLayer;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public void move(){
        switch (direction) {
            case UP -> worldY -= speed;
            case DOWN -> worldY += speed;
            case LEFT -> worldX -= speed;
            case RIGHT -> worldX += speed;
        }
    }

    public void update() {
        // reset collision and movement logic
        collisionOn = false;
    }
    
    public boolean isCollisionOn() {
        return collisionOn;
    }


    // GETTER AND SETTER
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public int getSpeed() { return speed; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public int getCurrentLayer(){ return currentLayer; }
    public void setCurrentLayer(int layer){ this.currentLayer = layer; }
    public Rectangle getSolidArea() { return solidArea; }
    public void setSolidArea(Rectangle solidArea) { this.solidArea = solidArea; }
}
