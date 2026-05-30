package model.object;

import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;

import java.awt.Rectangle;

/**
 * Base abstract class for all world objects.
 * Holds minimal shared spatial and collision data.
 */
public abstract class GameObject {

    protected int worldX, worldY;
    protected int width, height, layer;
    protected Rectangle solidArea;
    protected final Rectangle worldBoundsInstance = new Rectangle();
    
    protected boolean solid;
    protected boolean removed;
    protected String name;

    public GameObject(String name, int worldX, int worldY, int width, int height, Rectangle solidArea) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
        this.solidArea = solidArea != null ? solidArea : new Rectangle(0, 0, width, height);
        this.solid = true;
        this.removed = false;
    }

    public abstract void update(double deltaMs);

    public Rectangle getSolidWorldArea() {
        worldBoundsInstance.setBounds(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
        return worldBoundsInstance;
    }

<<<<<<< HEAD
    // Standard Getters
    public String getName() { return name; }
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isSolid() { return solid; }
    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
    public Rectangle getSolidArea() { return solidArea; }
}
=======
    // GETTER
    public boolean isSolid() {
        return solid;
    }
    public boolean isRemoved() {
        return removed;
    }
    public Rectangle getSolidArea() {
        return solidArea;
    }
    public Rectangle getSolidWorldArea() {
        return new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
    }
    public String getName() {
        return name;
    }
    public int getWorldX() {
        return worldX;
    }
    public int getWorldY() {
        return worldY;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

}
//-------------------------------------------------------------------------------------------------------------------
>>>>>>> bac3b41036aa45cbb8eab6f76da91da96e35b486
