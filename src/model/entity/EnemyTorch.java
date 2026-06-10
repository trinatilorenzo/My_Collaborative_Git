package model.entity;

import java.awt.Rectangle;

import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.DynamiteState;
import main.CONFIG.enu.TorchState;

/**
 * The EnemyTorch CLASS represents a melee enemy that chases the player
 * and attacks using a short-range flame attack.
 */
//-------------------------------------------------------------------------------------------------------------------
public class EnemyTorch extends Entity {

    // State
    private TorchState state;

    // Timers
    private double stateTimer;
    private double attackCooldownMs;

    // Movement
    private Direction facingDirection;

    // Attack
    private boolean attackDamageApplied;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public EnemyTorch(SpawnPoint spawnPoint, EntityConfig entityConfig) {
        super(entityConfig);
        initializeDefaultValues(spawnPoint);
    }
    //-------------------------------------------------------------

    /**
     * Initializes the enemy starting values.
     */
    //-------------------------------------------------------------
    public void initializeDefaultValues(SpawnPoint spawnPoint) {

        this.state = TorchState.APPROACH;

        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();

        this.speed = EntityConfig.TORCH_START_SPEED;

        this.maxLife = EntityConfig.TORCH_MAX_LIFE;
        this.life = maxLife;

        this.attackDamageApplied = false;

        this.solidArea = new Rectangle(
            0,
            0,
            EntityConfig.TORCH_HITBOX_WIDTH,
            EntityConfig.TORCH_HITBOX_HEIGHT
        );

        this.direction = Direction.DOWN;
        this.facingDirection = Direction.RIGHT;
    }
    //-------------------------------------------------------------

    /**
     * Updates the enemy state and movement.
     */
    //-------------------------------------------------------------
    public void update(Player player, double deltaMs) {

        super.update();

        if (attackCooldownMs > 0) {
            attackCooldownMs -= deltaMs;
        }

        stateTimer += deltaMs;

        facePlayer(player);

        switch (state) {

            case APPROACH:
                updateApproachState(player, deltaMs);
                break;

            case ATTACK_COMBO:
                updateAttackState(player);
                break;

            case DASH:
                updateDashState(player, deltaMs);
                break;

            case RECOVERY:
                updateRecoveryState();
                break;

            case GUARD:
                updateGuardState();
                break;

            case DEAD:
                break;
        }

        updateMovementDirection();
    }
    //-------------------------------------------------------------

    /**
     * Standard chase behaviour.
     */
    //-------------------------------------------------------------
    private void updateApproachState(Player player, double deltaMs) {

        double dxPlayer = player.getWorldX() - this.worldX; //distance in x
        double dyPlayer = player.getWorldY() - this.worldY; //distance in y
        double distance = Math.sqrt(dxPlayer * dxPlayer + dyPlayer * dyPlayer);

        if (distance < EntityConfig.TORCH_MELEE_RANGE) {

            state = TorchState.ATTACK_COMBO;
            stateTimer = 0;

            attackDamageApplied = false;

        } else if ( distance > EntityConfig.TORCH_DASH_RANGE_TRIGGER && attackCooldownMs <= 0) {
            state = TorchState.DASH;
            stateTimer = 0;

        } else {
            moveTowardsPlayer(player, deltaMs);
        }
    }
    //-------------------------------------------------------------

    /**
     * Handles flame attack logic.
     */
    //-------------------------------------------------------------
    private void updateAttackState(Player player) {

        if (!attackDamageApplied) {

            Rectangle flameArea = getAttackArea();

            Rectangle playerHitbox = player.getSolidWorldArea();

            if (flameArea.intersects(playerHitbox)) {
                player.takeDamage();
            }

            attackDamageApplied = true;
        }

        // Temporary timer until attack animation callbacks are implemented
        if (stateTimer >= 600) {
            completeAttackAnimation();
        }
    }
    //-------------------------------------------------------------

    /**
     * Executes dash behaviour.
     */
    //-------------------------------------------------------------
    private void updateDashState(Player player, double deltaMs) {
        moveTowardsPlayer(player, deltaMs);
        this.speed = EntityConfig.TORCH_DASH_SPEED;
        if (stateTimer >= 400) {
            state = TorchState.RECOVERY;
            stateTimer = 0;
        }
    }
    //-------------------------------------------------------------

    /**
     * Recovery period after an attack or dash.
     */
    //-------------------------------------------------------------
    private void updateRecoveryState() {

        if (stateTimer >= 1000) {

            state = TorchState.APPROACH;
            stateTimer = 0;

            attackCooldownMs = 2000;
        }
    }
    //-------------------------------------------------------------

    /**
     * Defensive state.
     */
    //-------------------------------------------------------------
    private void updateGuardState() {

        if (stateTimer >= 1500) {

            state = TorchState.APPROACH;
            stateTimer = 0;
        }
    }
    //-------------------------------------------------------------

    /**
     * Moves the enemy towards the player.
     */
    //-------------------------------------------------------------
    private void moveTowardsPlayer(Player player, double deltaMs) {

        // distance from the player
        double dxPlayer = player.getWorldX() - this.worldX; //distance in x
        double dyPlayer = player.getWorldY() - this.worldY; //distance in y
        double distance = Math.sqrt(dxPlayer * dxPlayer + dyPlayer * dyPlayer);

        if (distance > 0) {

            double moveDistance = speed * (deltaMs / 1000.0);

            dx = (int)Math.round((dxPlayer / distance) * moveDistance);
            dy = (int)Math.round((dyPlayer / distance) * moveDistance);
        }
    }
    //-------------------------------------------------------------

    /**
     * Updates the primary movement direction used by the collision system.
     */
    //-------------------------------------------------------------
    private void updateMovementDirection() {

        if (dx == 0 && dy == 0) {
            return;
        }

        if (Math.abs(dx) > Math.abs(dy)) {

            direction = (dx > 0)? Direction.RIGHT: Direction.LEFT;

        } else {

            direction = (dy > 0)? Direction.DOWN: Direction.UP;
        }
    }
    //-------------------------------------------------------------

    /**
     * Generates the flame attack area.
     */
    //-------------------------------------------------------------
    public Rectangle getAttackArea() {

        Rectangle attackArea = new Rectangle();

        int flameRange = EntityConfig.RANGE_ATTACK;

        attackArea.width = solidArea.width + flameRange;
        attackArea.height = solidArea.height + flameRange;

        int hitboxLeft = worldX - solidArea.width / 2;
        int hitboxTop = worldY - solidArea.height / 2;

        switch (direction) {

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
    //-------------------------------------------------------------

    /**
     * Applies damage to the enemy.
     */
    //-------------------------------------------------------------
    public void takeDamage() {

        if (state == TorchState.DEAD) {
            return;
        }

        if (state == TorchState.GUARD) {
            return;
        }

        life--;

        if (life <= 0) {
            state = TorchState.DEAD;
        }
    }
    //-------------------------------------------------------------

    /**
     * Completes the attack animation and enters recovery.
     */
    //-------------------------------------------------------------
    public void completeAttackAnimation() {

        if (state == TorchState.ATTACK_COMBO) {

            state = TorchState.RECOVERY;
            stateTimer = 0;
        }
    }
    //-------------------------------------------------------------

    /**
     * Updates visual facing direction.
     */
    //-------------------------------------------------------------
    private void facePlayer(Player player) {

        facingDirection =
            (player.getWorldX() >= worldX)
                ? Direction.RIGHT
                : Direction.LEFT;
    }
    //-------------------------------------------------------------

    /**
     * Returns the distance from the player.
     */
    //-------------------------------------------------------------

    // GETTERS ----------------------------------------------------

    public TorchState getState() {
        return state;
    }

    public boolean isFacingRight() {
        return facingDirection == Direction.RIGHT;
    }

    public boolean isDead() {
        return state == TorchState.DEAD;
    }

    //-------------------------------------------------------------
}