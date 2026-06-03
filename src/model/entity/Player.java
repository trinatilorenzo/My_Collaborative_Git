package model.entity;

import controller.InputState;
import main.CONFIG.EntityConfig;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.PlayerColor;
import main.CONFIG.enu.PlayerState;

import java.awt.Rectangle;

/**
 * The PLAYER CLASS represents the main player character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    private PlayerState state;
    private Direction facingDirection;

    private boolean deathAnimationCompleted;
    private boolean attackAnimationCompleted;

    private boolean attackDamageApplied;


    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public Player(EntityConfig entityConfig) {
        super(entityConfig);

        // Initialize the player's solid area for collision detection
        solidArea = new Rectangle(0,0, EntityConfig.PLAYER_HITBOX_WIDTH, EntityConfig.PLAYER_HITBOX_HEIGHT);
        initializeDefaultValues();

    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void initializeDefaultValues() {
        // Game start position
        worldX = entityConfig.START_WORLD_X() ;
        worldY = entityConfig.START_WORLD_Y() ;
        currentLayer = entityConfig.START_WORLD_LAYER();

        // Initialize movement values
        speed = EntityConfig.START_PLAYER_SPEED;
        direction = EntityConfig.START_FACING;
        facingDirection = EntityConfig.START_FACING;

        //state
        state = EntityConfig.PLAYER_DEFAULT_STATE;
        maxLife = EntityConfig.PLAYER_MAX_LIFE;
        life = maxLife;
        deathAnimationCompleted = false;
        attackAnimationCompleted = true;
        attackDamageApplied = false;

    }
    //-------------------------------------------------------------

    /**
     * Updates the player's state and movement each frame based on input
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {
        super.update(); // reset dx, dy, collisions

        // player DEAD no update
        if (state == PlayerState.DYING || state == PlayerState.DEAD) {
            return;
        }


        boolean isMoving = false;
        if (state != PlayerState.ATTACKING) {
            // move the player only if he is not attacking
            isMoving = updateMovement(input, deltaMs);
        }
        // update the player's state
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
        double distance = speed * (deltaMs / 1000.0); //distance to move in the current frame

        double moveX = 0;
        double moveY = 0;

        if (input.up())    { moveY -= distance; direction = Direction.UP; }
        if (input.down())  { moveY += distance; direction = Direction.DOWN; }
        if (input.left())  { moveX -= distance; direction = Direction.LEFT;  facingDirection = Direction.LEFT; }
        if (input.right()) { moveX += distance; direction = Direction.RIGHT; facingDirection = Direction.RIGHT; }

        fullSpeedX = (int) Math.round(moveX);
        fullSpeedY = (int) Math.round(moveY);

        // Normalize to mantain same speed in diagonal movements
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
        // While an attack animation is running, keep the state locked until renderer closes it.
        if (state == PlayerState.ATTACKING) {
            return;
        }

        if (input.attack()) {
            state = PlayerState.ATTACKING;
            attackAnimationCompleted = false;
            attackDamageApplied = false;
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
        attackArea.width = solidArea.width + EntityConfig.RANGE_ATTACK;
        attackArea.height = solidArea.height + EntityConfig.RANGE_ATTACK;
        // worldX/Y = center of solid area
        int hitboxLeft = worldX - solidArea.width / 2;
        int hitboxTop = worldY - solidArea.height / 2;

        switch(direction) {
            case UP:
                attackArea.x = worldX - attackArea.width / 2;
                attackArea.y = hitboxTop - attackArea.height;
                break;
            case DOWN:
                attackArea.x = worldX - attackArea.width / 2;
                attackArea.y = hitboxTop + solidArea.height;
                break;
            case LEFT:
                attackArea.x = hitboxLeft - attackArea.width;
                attackArea.y = worldY - attackArea.height / 2;
                break;
            case RIGHT:
                attackArea.x = hitboxLeft + solidArea.width;
                attackArea.y = worldY - attackArea.height / 2;
                break;
        }
        return attackArea;
    }
    //--------------------------------------------------------------

    /**
     * Reduce the player's life by 1 hp.
     * If the player's life reaches 0, it is considered dead.
     */
    //--------------------------------------------------------------
    public void takeDamage() {
        life --;
        // the player is dying
        if (life <= 0) {
            life = 0;
            state = PlayerState.DYING;
            deathAnimationCompleted = false;
        }
    }
    //--------------------------------------------------------------

    /**
     * set the end of death and player state to dead
     */
    //--------------------------------------------------------------
    public void completeDeathAnimation() {
        if (state == PlayerState.DYING) {
            state = PlayerState.DEAD;
            deathAnimationCompleted = true;
        }
    }
    //--------------------------------------------------------------

    /**
     * set the end of attack and player state to idle
     */
    //--------------------------------------------------------------
    public void completeAttackAnimation() {
        if (state == PlayerState.ATTACKING) {
            state = PlayerState.IDLE;
            attackAnimationCompleted = true;
        }
    }
    //--------------------------------------------------------------

    // GETTER ----------------------
    public PlayerState getState() {
        return state;
    }
    public Direction getFacing() {
        return facingDirection;
    }
    public boolean isDying() {
        return state == PlayerState.DYING;
    }
    public boolean isDead() {
        return state == PlayerState.DEAD;
    }
    public boolean isDeathAnimationCompleted() {
        return deathAnimationCompleted;
    }
    public boolean isAttackAnimationCompleted() {
        return attackAnimationCompleted;
    }
    public boolean isAttackDamageApplied() {
        return attackDamageApplied;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setState(PlayerState state) {
        this.state = state;
        if (state == PlayerState.DYING) {
            deathAnimationCompleted = false;
        }
        if (state == PlayerState.ATTACKING) {
            attackAnimationCompleted = false;
        } else {
            attackAnimationCompleted = true;
        }
    }
    public void setAttackDamageApplied(boolean applied) { 
        this.attackDamageApplied = applied; 
    }
    //---------------------------------




}
//-------------------------------------------------------------------------------------------------------------------
