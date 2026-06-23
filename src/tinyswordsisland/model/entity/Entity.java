package tinyswordsisland.model.entity;
import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.model.enu.Direction;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.RenderableType;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * The ENTITY CLASS serves as the base for all game entities by providing common properties
 * and functionality such as position, movement, collision handling, and layer management.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Entity implements Serializable, IRenderable {

    private static final long serialVersionUID = 1L;

    // SettingsMenu
    protected transient EntityConfig entityConfig;

    // Position and movement
    protected static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);
    protected int worldX, worldY; //center of the entity
    protected int speed; // PIXELS PER SECOND
    protected int dx, dy; // movement deltas per frame
    protected int fullSpeedX, fullSpeedY; // the full speed in each axis (used for diagonal movement)
    protected Direction direction;
    protected int currentLayer;

    // Health
    protected int life;
    protected int maxLife;

    // Hitbox and collision
    protected Rectangle solidArea;
    protected final Rectangle worldBoundsInstance = new Rectangle();

    protected boolean collisionX = false;
    protected boolean collisionY = false;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public Entity(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }
    //-------------------------------------------------------------

    /**
     * Resets per-frame movement state. Called at the start of each update.
     */
    //-------------------------------------------------------------
    protected void update() {
        dx = 0;
        dy = 0;
        fullSpeedX = 0;
        fullSpeedY = 0;
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
            // the player is moving diagonally
            if (collisionY) {
                // the player is moving horizontally
                appliedDx = fullSpeedX;
            }
            if (collisionX) {
                // the player is moving vertically
                appliedDy = fullSpeedY;
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
    public int getFullSpeedX() {
        return fullSpeedX;
    }
    public int getFullSpeedY() {
        return fullSpeedY;
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
    public int getLife() {
        return life;
    }
    public int getMaxLife() {
        return maxLife;
    }
    public int getWidth(){
        return solidArea.width;
    }
    public int getHeight(){
        return solidArea.height;
    }
    @Override
    public int getRenderLayer() {
        return currentLayer;
    }
    @Override
    public RenderableType getRenderableType() {
        return RenderableType.GAME_OBJECT;
    }
    // worldx and worldy centered in the solid area
    public Rectangle getSolidWorldArea() {
        worldBoundsInstance.setBounds(
                worldX - solidArea.width / 2,
                worldY - solidArea.height / 2,
                solidArea.width,
                solidArea.height
        );
        return worldBoundsInstance;
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
    public void setLife(int life) {
        this.life = life;
    }
    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------