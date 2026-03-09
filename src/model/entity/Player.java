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

        solidArea = new Rectangle(79, 88, 40, 40);
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
        screenX = SCREEN_WIDTH / 2 - TILE_SIZE / 2;
        screenY = SCREEN_HEIGHT / 2 - TILE_SIZE / 2;
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
    public void update(KeyHandler keyH) {
        super.update(); // reset dx, dy, collisions

        boolean isMoving = updateMovement(keyH);
        updateState(keyH, isMoving);
    }
    //-------------------------------------------------------------

    /**
     * Reads all directional keys simultaneously, accumulates dx/dy,
     * and normalizes for diagonal movement to keep constant speed.
     */

    //-------------------------------------------------------------
    private boolean updateMovement(KeyHandler keyH) {
        boolean isMoving = false;

        if (keyH.isUp()) {
            dy -= speed;
            direction = Direction.UP;
            isMoving = true;
        }
        if (keyH.isDown()) {
            dy += speed;
            direction = Direction.DOWN;
            isMoving = true;
        }
        if (keyH.isLeft()) {
            dx -= speed;
            direction = Direction.LEFT;
            facingDirection = FACING_LEFT;
            isMoving = true;
        }
        if (keyH.isRight()) {
            dx += speed;
            direction = Direction.RIGHT;
            facingDirection = FACING_RIGHT;
            isMoving = true;
        }

        // Normalize diagonal movement to keep constant speed
        if (dx != 0 && dy != 0) {
            dx = (int) Math.round(dx * DIAGONAL_FACTOR);
            dy = (int) Math.round(dy * DIAGONAL_FACTOR);
        }

        return isMoving;
    }
    //-------------------------------------------------------------

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