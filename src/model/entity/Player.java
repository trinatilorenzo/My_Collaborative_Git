package model.entity;

import controller.KeyHandler;

import java.awt.Rectangle;

import static main.GameSetting.*;

/**
 * The PLAYER CLASS represents the main player character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    private static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);

    private PlayerState state;
    private int facingDirection;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public Player() {
        // TODO: Make it parametric

        solidArea = new Rectangle((SPRITE_FRAME_WIDTH / 2) - (PLAYER_HITBOX_WIDTH/2),
                (SPRITE_FRAME_HEIGHT/ 2) ,
                PLAYER_HITBOX_WIDTH,
                PLAYER_HITBOX_HEIGHT);

        initializeDefaultValues();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void initializeDefaultValues() {
        // Game start position
        worldX = START_WORLD_X;
        worldY = START_WORLD_Y;
        currentLayer = START_WORLD_LAYER;

        // Screen position
        screenX = SCREEN_WIDTH / 2 - PLAYER_RENDER_WIDTH/ 2;
        screenY = SCREEN_HEIGHT / 2 - PLAYER_RENDER_HEIGHT / 2;

        // Initialize movement values
        speed = START_PLAYER_SPEED;
        direction = Direction.RIGHT;
        state = PlayerState.IDLE;
        facingDirection = FACING_RIGHT;
    }
    //-------------------------------------------------------------

    /**
     * Updates the player's state and movement each frame based on input
     * received from the {@link KeyHandler}.
     */

    //-------------------------------------------------------------
    public void update(KeyHandler keyH, double deltaMs) {
        super.update(); // reset dx, dy, collisions

        boolean isMoving = updateMovement(keyH, deltaMs);
        updateState(keyH, isMoving);
    }
    //-------------------------------------------------------------

    /**
     * Reads all directional keys simultaneously, accumulates dx/dy,
     * and normalizes for diagonal movement to keep constant speed.
     */

    //-------------------------------------------------------------

    private boolean updateMovement(KeyHandler keyH, double deltaMs) {
        double distance = speed * (deltaMs / 1000.0);
        double moveX = 0;
        double moveY = 0;

        if (keyH.isUp())    { moveY -= distance; direction = Direction.UP; }
        if (keyH.isDown())  { moveY += distance; direction = Direction.DOWN; }
        if (keyH.isLeft())  { moveX -= distance; direction = Direction.LEFT;  facingDirection = FACING_LEFT; }
        if (keyH.isRight()) { moveX += distance; direction = Direction.RIGHT; facingDirection = FACING_RIGHT; }

        // Normalizza per mantenere la stessa velocità anche in diagonale (fattore 1/sqrt(2))
        if (moveX != 0 && moveY != 0) {
            moveX *= DIAGONAL_FACTOR;
            moveY *= DIAGONAL_FACTOR;
        }

        dx = (int) Math.round(moveX);
        dy = (int) Math.round(moveY);

        return moveX != 0 || moveY != 0;
    }


    /**
     * Updates the player's current state based on input from the {@link KeyHandler}
     * and movement status.
     */

    //-------------------------------------------------------------
    private void updateState(KeyHandler keyH, boolean isMoving) {
        if (keyH.isAttack()) {
            state = PlayerState.ATTACKING;
        } else if (isMoving) {
            state = PlayerState.WALKING;
        } else {
            state = PlayerState.IDLE;
        }
    }
    //-------------------------------------------------------------



    // GETTER ----------------------
    public PlayerState getState() {
        return state;
    }
    public int getFacingRight() {
        return facingDirection;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setState(PlayerState state) {
        this.state = state;
    }
    public void stopAttack() {
        state = PlayerState.IDLE;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
