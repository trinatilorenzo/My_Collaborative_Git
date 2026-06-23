package tinyswordsisland.model.object;

import java.awt.Rectangle;
import tinyswordsisland.config.ObjConfig;
import tinyswordsisland.config.SpawnPoint;
import tinyswordsisland.model.enu.PowerUpType;

/**
 * Represents collectible power-up items in the game world 
 * that provide temporary benefits to the player.
 */
//-------------------------------------------------------------------------------------------------------------------
public class OBJ_PowerUp extends GameObject {
    private final PowerUpType type;
    private final long spawnTime;
    private static final long pickupDuration = ObjConfig.PICKUP_DURATION_MS; // Duration of the power-up effect after being picked up

    /**
     * CONSTRUCTOR
     */
    public OBJ_PowerUp(ObjConfig objConfig, PowerUpType type, int x, int y, int layer) {
        super(objConfig, type.name(), new SpawnPoint(x, y, layer), objConfig.POWER_UP_SIZE, objConfig.POWER_UP_SIZE, new Rectangle(0, 0, objConfig.POWER_UP_HITBOX_SIZE, objConfig.POWER_UP_HITBOX_SIZE), false);
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }

    public boolean isCollectible() {
        return (System.currentTimeMillis() - spawnTime) >= pickupDuration;
    }

    public PowerUpType getType() {
        return type;
    }
    @Override
    public String getRenderVariant() {
        return type.name();
    }

}
