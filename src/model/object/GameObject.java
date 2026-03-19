package model.object;

import java.awt.Rectangle;

/**
 * The GAME OBJECT CLASS serves as the base for all interactive objects in the game world, providing common properties such as position, collision handling, and rendering information.
 */
public class GameObject {

    protected int worldX, worldY;
    protected int width, height;
    
    protected Rectangle solidArea;
    
    protected boolean solid;
    protected boolean removed;

    protected String name;

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
