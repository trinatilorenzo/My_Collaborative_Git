package tinyswordsisland.model.entity;

import tinyswordsisland.controller.InputState;
import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.enu.Direction;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.config.enu.PlayerState;
import tinyswordsisland.config.enu.PowerUpType;

import java.awt.Rectangle;

/**
 * The PLAYER CLASS represents the main player character in the game, extending the base Entity class.
 */
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    private PlayerState state;
    private Direction facingDirection;
    private PlayerColor color;
    private InputState input; // current input

    private boolean deathAnimationCompleted;
    private boolean attackAnimationCompleted;

    private boolean attackDamageApplied;

    // handle power-ups effects
    private double shieldTimerMs = 0;
    private boolean isShielded = false;
    private boolean hasShield = false;
    private boolean isSpeedBoosted = false;
    private boolean isHealthRestored = false;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public Player(EntityConfig entityConfig, PlayerColor color) {
        super(entityConfig);
        // Initialize the player's solid area for collision detection
        solidArea = new Rectangle(0,0, EntityConfig.PLAYER_HITBOX_WIDTH, EntityConfig.PLAYER_HITBOX_HEIGHT);
        initializeDefaultValues();
        this.color = color;

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

        color = entityConfig.DEFAULT_COLOR;

    }
    //-------------------------------------------------------------

    /**
     * Riceve l'input dal controller PRIMA dell'aggiornamento logico
     */
    public void handleInput(InputState input) {
        this.input = input;
    }
    /**
     * Updates the player's state and movement each frame based on input
     */
    //-------------------------------------------------------------
    @Override 
    public void update(double deltaMs) {
        super.resetFrameState(); // reset dx, dy, collisions

        // player DEAD no update
        if (state == PlayerState.DYING || state == PlayerState.DEAD) {
            return;
        }

        // update shield timer
        if (shieldTimerMs > 0) {
            shieldTimerMs -= deltaMs;
            isShielded = true;
            if (shieldTimerMs <= 0) {
                shieldTimerMs = 0;
                isShielded = false;
                hasShield = false;
            }
        }else {
            isShielded = false;
        }

        if (input != null) {
            boolean isMoving = updateMovement(input, deltaMs);
            updateState(input, isMoving);
        }
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
        if (state == PlayerState.ATTACKING) {
            if (input.movementRequested()) {
                state = PlayerState.WALKING;
                attackAnimationCompleted = true;
                attackDamageApplied = true;
            }
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
        Rectangle body = getSolidWorldArea();
        Rectangle attackArea = new Rectangle();

        attackArea.width = body.width + EntityConfig.RANGE_ATTACK;
        attackArea.height = body.height + EntityConfig.RANGE_ATTACK;

        switch (direction) {
            case UP -> {
                attackArea.x = body.x + body.width / 2 - attackArea.width / 2;
                attackArea.y = body.y - attackArea.height;
            }
            case DOWN -> {
                attackArea.x = body.x + body.width / 2 - attackArea.width / 2;
                attackArea.y = body.y + body.height;
            }
            case LEFT -> {
                attackArea.x = body.x - attackArea.width;
                attackArea.y = body.y + body.height / 2 - attackArea.height / 2;
            }
            case RIGHT -> {
                attackArea.x = body.x + body.width;
                attackArea.y = body.y + body.height / 2 - attackArea.height / 2;
            }
        }

        return attackArea;
    }
    //--------------------------------------------------------------

    /**
     * Reduce the player's life by 1 hp.
     * If the player's life reaches 0, it is considered dead.
     */
    //--------------------------------------------------------------
    @Override
    public void takeDamage() {
        if (isShielded) {
            // player is shielded, ignore damage
            return;
        }
        super.takeDamage();
        // the player is dying
        if (life == 0) {
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

    /**
     * Apply the effect of a collected power-up to the player.
     */
    public void applyPowerUpEffect(PowerUpType type) {
        switch (type) {
            case SHIELD:
                this.shieldTimerMs = EntityConfig.SHIELD_DURATION_MS;
                this.isShielded = false;
                this.hasShield = true;
                break;
            case HEALTH_RESTORE:
                this.life = this.maxLife;
                this.isHealthRestored = true; 
                break;
            case SPEED_BOOST:
                this.isSpeedBoosted = true;
                this.speed += EntityConfig.SPEED_BOOST_AMOUNT;
                break;
        }
    }
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
    public PlayerColor getColor() {return color;}
    public boolean isShielded() {
        return isShielded;
    }
    public boolean isSpeedBoosted() {
        return isSpeedBoosted;
    }
    public boolean isHealthRestored() {
        return isHealthRestored;
    }
    public double getShieldTimerMs() {
        return shieldTimerMs;
    }
    public boolean hasShield() {
        return hasShield;
    }

    @Override
    public Rectangle getSolidWorldArea() {
        worldBoundsInstance.setBounds(
            worldX - solidArea.width / 2,
            worldY - solidArea.height / 2 + EntityConfig.PLAYER_OFFSET_HEIGHT,
            solidArea.width,
            solidArea.height
        );
        return worldBoundsInstance;
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
    public void setColor(PlayerColor color) {this.color = color;    }
    //---------------------------------




}
//-------------------------------------------------------------------------------------------------------------------
