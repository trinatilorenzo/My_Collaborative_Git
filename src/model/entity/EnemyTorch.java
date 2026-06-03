package model.entity;

import java.awt.Rectangle;
import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.TorchState;

/**
 * The EnemyTorch class represents a enemy that fight against the player
 */
//-------------------------------------------------------------------------------------------------------------------
public class EnemyTorch extends Entity {

    private TorchState state;
    private double stateTimer;
    private double attackCooldownMs;
    private Direction facingDirection;

    private boolean attackAnimationCompleted;
    private boolean attackDamageApplied;

    /**
     * COSTRUCTOR
     */
    //-------------------------------------------------------------
    public EnemyTorch(SpawnPoint spawnPoint, EntityConfig entityConfig) {
        super(entityConfig);
        initializeDefaultValues(spawnPoint);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void initializeDefaultValues(SpawnPoint spawnPoint) {
        this.state = TorchState.APPROACH;
        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();
        
        this.speed = EntityConfig.TORCH_START_SPEED; 
        this.life = EntityConfig.TORCH_MAX_LIFE; 
        this.maxLife = EntityConfig.TORCH_MAX_LIFE; 
        
        this.attackAnimationCompleted = true;
        this.attackDamageApplied = false;
       
        solidArea = new Rectangle(0, 0, EntityConfig.TORCH_HITBOX_WIDTH, EntityConfig.TORCH_HITBOX_HEIGHT);
    }
    //-------------------------------------------------------------


    /**
     * Updates the enemy state and movement
     */
    //-------------------------------------------------------------
    public void update(Player player, double deltaMs) {
        super.update();

        if (attackCooldownMs > 0) attackCooldownMs -= deltaMs;
        stateTimer += deltaMs;

        facePlayer(player); // Face always the player 

        switch (state) {
            case APPROACH:
                double dist = getDistanceToPlayer(player);
                if (dist < EntityConfig.TORCH_MELEE_RANGE) {
                    // if too close, start attacking
                    state = Math.random() > 0.5 ? TorchState.GUARD : TorchState.ATTACK_COMBO;
                    stateTimer = 0;
                } else if (dist > EntityConfig.TORCH_DASH_RANGE_TRIGGER && attackCooldownMs <= 0) {
                    // if player is far enough, start dash
                    state = TorchState.DASH;
                    stateTimer = 0;
                } else {
                    moveTowardsPlayer(player, deltaMs);
                }
                break;

            case GUARD:
                //Stay in guard mode for 1.5 seconds
                if (stateTimer >= 1500) {
                    state = TorchState.ATTACK_COMBO;
                    stateTimer = 0;
                }
                break;

            case ATTACK_COMBO:
                // Execute a combo attack (the rendere will play the animation)
                executeComboLogic(player);
                break;

            case DASH:
                // fast run towards player
                executeDashLogic(player, deltaMs);
                break;

            case RECOVERY:
                // stil in recovery mode for 1 second (vurneable by palayer)
                if (stateTimer >= 1000) {
                    state = TorchState.APPROACH;
                    attackCooldownMs = 2000; // Cooldown prima del prossimo attacco pesante
                    stateTimer = 0;
                }
                break;

            case DEAD:
                break;
        }
        //System.out.println("Torch State: " + state + " | Life: " + life);
    }
    //-------------------------------------------------------------

    /**
    * UTILITY METHODS
     */
    //-------------------------------------------------------------
    public void takeDamage() {
        if (state == TorchState.DEAD) return;

        // knock back
        if (state == TorchState.GUARD) {
            // Qui potresti triggerare un evento audio di "Scudo/Parata" 
            System.out.println("Torch ha parato il colpo!");
            return; 
        }

        this.life--;
        if (this.life <= 0) {
            this.state = TorchState.DEAD;
        }
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private double getDistanceToPlayer(Player player) {
        long dx = player.getWorldX() - worldX;
        long dy = player.getWorldY() - worldY;
        return Math.sqrt(dx * dx + dy * dy);
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void moveTowardsPlayer(Player player, double deltaMs) {
        double dxPlayer = player.getWorldX() - worldX;
        double dyPlayer = player.getWorldY() - worldY;
        double distance = getDistanceToPlayer(player);

        if (distance > 0) {
            double dist = speed * (deltaMs / 1000.0);
            dx = (int) Math.round((dxPlayer / distance) * dist);
            dy = (int) Math.round((dyPlayer / distance) * dist);
        }
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void facePlayer(Player player) {
        this.facingDirection = (player.getWorldX() >= worldX) ? Direction.RIGHT : Direction.LEFT;
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void executeDashLogic(Player player, double deltaMs) {
        // Aumenta temporaneamente la velocità per fare uno scatto
        // Se colpisce il player fa danno, poi passa in RECOVERY
        if (stateTimer >= 400) { // Il dash dura 400ms
            state = TorchState.RECOVERY;
            stateTimer = 0;
        }
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void executeComboLogic(Player player) {

        if (!attackDamageApplied) {

            if (attackHitsPlayer(player)) {
                player.takeDamage();
            }

            attackDamageApplied = true;
        }

    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private boolean attackHitsPlayer(Player player) {
        return getDistanceToPlayer(player) < EntityConfig.TORCH_MELEE_RANGE;
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    public void completeAttackAnimation() {
        // Questo metodo viene chiamato dal Renderer quando l'animazione di attacco finisce
        if (state == TorchState.ATTACK_COMBO) {

        attackAnimationCompleted = true;

        state = TorchState.RECOVERY;
        stateTimer = 0;
    }}
    //-------------------------------------------------------------
    // end utility methods ---------------------------------------

    // GETTERS
    //-------------------------------------------------------------
    public TorchState getState() { return state; }
    //-------------------------------------------------------------

    //SETTERS
    //-------------------------------------------------------------
    public boolean isFacingRight() { return facingDirection == Direction.RIGHT; }
    public boolean isDead() { return state == TorchState.DEAD; }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------