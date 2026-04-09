package model.entity;

import input.InputState;
import main.CONFIG.EntityConfig;
import main.ENUM.Direction;
import main.ENUM.PlayerState;

import java.awt.*;

public class EnemyTorch extends Entity{

    private Direction facingDirection;
    private PlayerState state;
    private EntityConfig entityConfig;

    public EnemyTorch(EntityConfig entityConfig) {
        //get the entityConfig
        this.entityConfig = entityConfig;
        // Initialize the player's solid area for collision detection
        solidArea = new Rectangle((entityConfig.SPRITE_WIDTH / 2) - (entityConfig.PLAYER_HITBOX_WIDTH/2),
                (entityConfig.SPRITE_HEIGHT / 2) ,
                entityConfig.PLAYER_HITBOX_WIDTH,
                entityConfig.PLAYER_HITBOX_HEIGHT);

        initializeDefaultValues();

    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void initializeDefaultValues() {
        // Game start position
        worldX = entityConfig.START_WORLD_X();
        worldY = entityConfig.START_WORLD_Y();
        currentLayer = entityConfig.START_WORLD_LAYER();


        // Initialize movement values
        speed = entityConfig.START_PLAYER_SPEED;
        direction = entityConfig.START_FACING;
        facingDirection = entityConfig.START_FACING;
        state = PlayerState.IDLE;
    }
    //-------------------------------------------------------------

    /**
     * Updates the player's state and movement each frame based on input
     */

    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {
        super.update(); // reset dx, dy, collisions

        boolean isMoving = false;
        // Durante l'attacco non aggiorniamo il movimento: resta fermo finché l'animazione non termina
        if (state != PlayerState.ATTACKING) {
            isMoving = updateMovement(input, deltaMs);
        }
        updateState(input, isMoving);
    }
    //-------------------------------------------------------------

    /**
     * Reads all directional keys simultaneously, accumulates dx/dy,
     * and normalizes for diagonal movement to keep constant speed.
     */
    //-------------------------------------------------------------
    private boolean updateMovement(InputState input, double deltaMs) {

    }

}
