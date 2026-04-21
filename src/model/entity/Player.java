package model.entity;

import controller.InputState;
import main.CONFIG.EntityConfig;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.PlayerState;


import java.awt.Rectangle;



/**
 * The PLAYER CLASS represents the main player character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    protected int screenX, screenY;
    private PlayerState state;
    private Direction facingDirection;
    private EntityConfig entityConfig;


    // COSTRUCTOR
    //-------------------------------------------------------------
    public Player(EntityConfig entityConfig) {
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

        // Screen position
        screenX = entityConfig.SCREEN_POSX();
        screenY = entityConfig.SCREEN_POSY();

        // Initialize movement values
        speed = entityConfig.START_PLAYER_SPEED;
        direction = entityConfig.START_FACING;
        facingDirection = entityConfig.START_FACING;
        state = PlayerState.IDLE;
        maxLife = 8;
        life = maxLife;
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
        //speed is pixel per second
        //deltaMs is the time elapsed since the last frame
        double distance = speed * (deltaMs / 1000.0); //distance is the distance to move in the current frame

        double moveX = 0;
        double moveY = 0;

        if (input.up())    { moveY -= distance; direction = Direction.UP; }
        if (input.down())  { moveY += distance; direction = Direction.DOWN; }
        if (input.left())  { moveX -= distance; direction = Direction.LEFT;  facingDirection = Direction.LEFT; }
        if (input.right()) { moveX += distance; direction = Direction.RIGHT; facingDirection = Direction.RIGHT; }

        intendedDx = (int) Math.round(moveX);
        intendedDy = (int) Math.round(moveY);

        // Normalizza per mantenere la stessa velocità anche in diagonale (fattore 1/sqrt(2))
        if (moveX != 0 && moveY != 0) {
            moveX *= DIAGONAL_FACTOR;
            moveY *= DIAGONAL_FACTOR;
        }

        dx = (int) Math.round(moveX);
        dy = (int) Math.round(moveY);

        return moveX != 0 || moveY != 0;
    }


    //-------------------------------------------------------------

    /**
     * Updates the player's current state based on input
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

    //TODO VEDERE BENE IL METODO
    //----------------------------------------------
    public Rectangle getAttackArea() {
        Rectangle attackArea = new Rectangle();
        attackArea.width = solidArea.width + entityConfig.RANGE_ATTACK;
        attackArea.height = solidArea.height + entityConfig.RANGE_ATTACK;
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

    //--------------------------------------------------------------
    // Methods to reduce a life 
    public void takeDamage() {
        life -= 1;
        System.out.println("Remaining life: " + life);
        if (life <= 0) {
            //TODO: Handle player death (e.g., trigger game over, respawn, etc.)
            life = 0;
        }
    }


    // GETTER ----------------------
    public PlayerState getState() {
        return state;
    }
    public Direction getFacing() {
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
