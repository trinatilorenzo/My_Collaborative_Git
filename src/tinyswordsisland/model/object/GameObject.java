package tinyswordsisland.model.object;

import tinyswordsisland.config.ObjConfig;
import tinyswordsisland.config.SpawnPoint;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.RenderableType;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * The GAME OBJECT CLASS serves as the base for all interactive objects in the game world,
 * providing common properties such as position, collision handling, and rendering information.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameObject implements Serializable, IRenderable {

    private static final long serialVersionUID = 1L;

    protected transient ObjConfig objConfig;

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
    @Override
    public int getRenderLayer() {
        return layer;
    }
    @Override
    public RenderableType getRenderableType() {
        return RenderableType.GAME_OBJECT;
    }
    @Override
    public String getRenderVariant() {
        return name == null ? "" : name;
    }
    @Override
    public boolean isRemovedRender() {
        return removed;
    }
    // SETTER
    public void remove() {
        this.removed = true;
    }
    public void setObjConfig(ObjConfig objConfig) {
        this.objConfig = objConfig;
    }
    public void setSolid(boolean value){
        this.solid = value;
    }

}
//-------------------------------------------------------------------------------------------------------------------
