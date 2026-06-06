package model.object;

import java.awt.Rectangle;
import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.PowerUpType;

/**
 * Represents collectible power-up items in the game world 
 * that provide temporary benefits to the player.
 */
//-------------------------------------------------------------------------------------------------------------------
public class OBJ_PowerUp extends GameObject {
    private final PowerUpType type;

    /**
     * CONSTRUCTOR
     */
    public OBJ_PowerUp(ObjConfig objConfig, PowerUpType type, int x, int y, int layer) {
        super(objConfig, type.name(), new SpawnPoint(x, y, layer), objConfig.POWER_UP_SIZE, objConfig.POWER_UP_SIZE, new Rectangle(0, 0, objConfig.POWER_UP_HITBOX_WIDTH, objConfig.POWER_UP_HITBOX_HEIGHT), false);
        this.type = type;
    }

    public PowerUpType getType() {
        return type;
    }

}
