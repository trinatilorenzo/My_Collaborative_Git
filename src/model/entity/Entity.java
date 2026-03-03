package model.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import model.enums.Direction;

public class Entity {

    protected int worldX, worldY;
    protected int speed;
    protected Rectangle solidArea;
    protected boolean collisionOn = false;
    public int currentLayer;

    public Direction direction;

    public BufferedImage warrior;

    public void update() {
        collisionOn = false;
    }
    
    public boolean isCollisionOn() {
        return collisionOn;
    }

    public void move(){
        switch (direction) {
            case UP -> worldY -= speed;
            case DOWN -> worldY += speed;
            case LEFT -> worldX -= speed;
            case RIGHT -> worldX += speed;
        }
    }

    // GETTER AND SETTER
    public int getCurrentLayer(){ return currentLayer; }
    public void setCurrentLayer(int layer){ this.currentLayer = layer; }

}
