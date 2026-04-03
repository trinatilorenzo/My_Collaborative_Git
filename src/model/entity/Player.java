package model.entity;

import input.InputState;
import main.GameSetting.Direction;
import main.GameSetting.PlayerState;

import java.awt.Rectangle;

import static main.GameSetting.*;

/**
 * The PLAYER CLASS represents the main player character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    protected int screenX, screenY;

    private static final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2);

    private PlayerState state;
    private int facingDirection;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public Player() {
        // Initialize the player's solid area for collision detection
        solidArea = new Rectangle((PLAYER_SPRITE_WIDTH / 2) - (PLAYER_HITBOX_WIDTH/2),
                (PLAYER_SPRITE_HEIGHT/ 2) ,
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
        screenX = SCREEN_WIDTH / 2 - PLAYER_SPRITE_WIDTH/ 2;
        screenY = SCREEN_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2;

        // Initialize movement values
        speed = START_PLAYER_SPEED;
        direction = Direction.RIGHT;
        state = PlayerState.IDLE;
        facingDirection = FACING_RIGHT;
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
        double distance = speed * (deltaMs / 1000.0);
        double moveX = 0;
        double moveY = 0;

        if (input.up())    { moveY -= distance; direction = Direction.UP; }
        if (input.down())  { moveY += distance; direction = Direction.DOWN; }
        if (input.left())  { moveX -= distance; direction = Direction.LEFT;  facingDirection = FACING_LEFT; }
        if (input.right()) { moveX += distance; direction = Direction.RIGHT; facingDirection = FACING_RIGHT; }

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
    private void updateState(InputState input, boolean isMoving) {
        if (state == PlayerState.ATTACKING) {
            return;
        }

        if (input.attack()) {
            state = PlayerState.ATTACKING;
        } else if (isMoving) {
            state = PlayerState.WALKING;
        } else {
            state = PlayerState.IDLE;
        }
    }
    //-------------------------------------------------------------

    //----------------------------------------------
    public Rectangle getAttackArea() {
        Rectangle attackArea = new Rectangle();
        attackArea.width = solidArea.width + RANGE_ATTACK; 
        attackArea.height = solidArea.height + RANGE_ATTACK;
        int hitboxX = worldX + solidArea.x;
        int hitboxY = worldY + solidArea.y;

        switch(direction) {
            case UP:
                attackArea.x = hitboxX + (solidArea.width / 2) - (attackArea.width / 2);
                attackArea.y = hitboxY - attackArea.height;
                break;
            case DOWN:
                attackArea.x = hitboxX + (solidArea.width / 2) - (attackArea.width / 2);
                attackArea.y = hitboxY + solidArea.height;
                break;
            case LEFT:
                attackArea.x = hitboxX - attackArea.width;
                attackArea.y = hitboxY + (solidArea.height / 2) - (attackArea.height / 2);
                break;
            case RIGHT:
                attackArea.x = hitboxX + solidArea.width;
                attackArea.y = hitboxY + (solidArea.height / 2) - (attackArea.height / 2);
                break;
        }
        return attackArea;
    }


    // GETTER ----------------------
    public PlayerState getState() {
        return state;
    }
    public int getFacingRight() {
        return facingDirection;
    }
    public int getScreenX() {
        return screenX;
    }
    public int getScreenY() {
        return screenY;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setState(PlayerState state) {
        this.state = state;
    }
    public void stopAttack() {
        state = PlayerState.IDLE;
    }
        public void setScreenPosition(int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
    }
    //---------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
