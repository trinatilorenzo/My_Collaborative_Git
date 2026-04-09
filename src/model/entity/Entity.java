package model.entity;
import main.ENUM.Direction;

import java.awt.Rectangle;

/**
 * The ENTITY CLASS serves as the base for all game entities by providing common properties
 * and functionality such as position, movement, collision handling, and layer management.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Entity {

    // Position and movement
    protected static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);

    protected int worldX, worldY;
    protected int speed; // PIXELS PER SECOND
    protected int dx, dy; // movement deltas per frame
    protected int life;
    protected int intendedDx, intendedDy;
    protected Direction direction;
    protected int currentLayer;

    // Hitbox and collision
    protected Rectangle solidArea;
    protected boolean collisionX = false;
    protected boolean collisionY = false;

    /**
     * Resets per-frame movement state. Called at the start of each update.
     */
    //-------------------------------------------------------------
    public void update() {
        dx = 0;
        dy = 0;
        intendedDx = 0;
        intendedDy = 0;
        collisionX = false;
        collisionY = false;
    }
    //-------------------------------------------------------------

    /**
     * Applies movement based on dx/dy, respecting per-axis collision.
     */

    //-------------------------------------------------------------
    public void move() {
        int appliedDx = dx;
        int appliedDy = dy;

        // normalized movement when coliding on a border
        if (dx != 0 && dy != 0) {
            // the paleyr is moving diagonally
            if (collisionY) {
                // the player is moving horizontally
                appliedDx = intendedDx;
            }
            if (collisionX) {
                // the player is moving vertically
                appliedDy = intendedDy;
            }
        }

        if (!collisionX) { 
            worldX += appliedDx;
        }
        if (!collisionY) {
            worldY += appliedDy;
        }
    }
    //-------------------------------------------------------------

    // GETTER ----------------------
    public boolean isCollisionX() {
        return collisionX;
    }
    public boolean isCollisionY() {
        return collisionY;
    }
    public boolean isCollisionOn() {
        return collisionX && collisionY;
    }
    public int getDx() {
        return dx;
    }
    public int getDy() {
        return dy;
    }
    public int getWorldX() {
        return worldX;
    }
    public int getWorldY() {
        return worldY;
    }
    public int getSpeed() {
        return speed;
    }
    public int getCurrentLayer() {
        return currentLayer;
    }
    public Direction getDirection() {
        return direction;
    }
    public Rectangle getSolidArea() {
        return solidArea;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    public void setLayer(int layer) {
        this.currentLayer = layer;
    }
    public void setSolidArea(Rectangle solidArea) {
        this.solidArea = solidArea;
    }
    public void setCollisionX(boolean collisionX) {
        this.collisionX = collisionX;
    }
    public void setCollisionY(boolean collisionY) {
        this.collisionY = collisionY;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }  
    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------