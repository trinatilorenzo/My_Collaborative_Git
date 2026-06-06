package model.object;

import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;

import java.awt.Rectangle;

/**
 * The GAME OBJECT CLASS serves as the base for all interactive objects in the game world,
 * providing common properties such as position, collision handling, and rendering information.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameObject {
    protected ObjConfig objConfig;

    protected String name;
    protected int worldX, worldY, layer; //position
    protected int width, height; // visual size
    
    protected Rectangle solidArea; // dimension
    protected final Rectangle worldBoundsInstance = new Rectangle();

    protected boolean solid;
    protected boolean removed;

    public GameObject(ObjConfig objConfig, String name, SpawnPoint spawnPoint, int width, int height, Rectangle solidArea, Boolean solid){
        this.objConfig = objConfig;
        worldX = spawnPoint.x();
        worldY = spawnPoint.y();
        layer = spawnPoint.layer();

        this.name = name;
        this.width = width;
        this.height = height;
        this.solidArea = solidArea;
        this.solid = solid;
    }

    public void interact() {
        // Default interaction behavior (can be overridden by subclasses)
    }

    public void update(double deltaMs){
        // Default update behavior (can be overridden by subclasses)
    }

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
        worldBoundsInstance.setBounds(
            worldX + solidArea.x,
            worldY + solidArea.y,
            solidArea.width,
            solidArea.height
        );
        return worldBoundsInstance;
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
    public int getLayer() {
        return layer;
    }
    // SETTER
    public void remove() {
        this.removed = true;
    }

}
//-------------------------------------------------------------------------------------------------------------------
