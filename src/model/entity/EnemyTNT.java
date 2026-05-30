package model.entity;

import java.awt.Rectangle;
import java.util.Random;

import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.TNTState;

/**
 * The EnemyTNT CLASS an NPC that explodes when the player is within a certain radius.
 */
//-------------------------------------------------------------------------------------------------------------------
public class EnemyTNT extends Entity{

    //state
    private TNTState state;
    private long triggerTimer;
    private long explosionTimer;

    //movement
    private double moveTimer; // Timer to control wandering movement
    private Random random;
    private double dirX; //save the current direction of TNT
    private double dirY;

    private boolean hasDealtDamage;  // Flag to ensure damage is applied only once per explosion


    /**
     * COSTRUCTOR
     */
    //--------------------------------------------------------------
    public EnemyTNT(SpawnPoint spawnPoint, EntityConfig entityConfig) {
        super(entityConfig);

        initializeDefaultValues(spawnPoint);
    }
    //--------------------------------------------------------------
    private void initializeDefaultValues(SpawnPoint spawnPoint) {
        this.state =  EntityConfig.TNT_DEFAULT_STATE;
        life = EntityConfig.TNT_MAX_LIFE;

        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();

        this.speed = EntityConfig.START_TNT_SPEED;

        solidArea = new Rectangle(0, 0, EntityConfig.TNT_HITBOX_WIDTH, EntityConfig.TNT_HITBOX_HEIGHT);

        random = new Random();
        moveTimer = 0;
        dirX = 0;
        dirY = 0;
        hasDealtDamage = false;
    }
    //--------------------------------------------------------------

    /**
     * Updates the enemy state and movement
     */
    //-----------------------------------------------------------------------
    public void update(Player player, double deltaMs) {
        super.update(); // Reset movement and collision states

        switch (state) {
            case WANDER: 
                wander(deltaMs);
                checkPlayerProximity(player);
                break;
            case TRIGGERED: 
                triggerTimer += deltaMs;
                if (triggerTimer >= EntityConfig.TNT_EXPLOSION_DELAY) {
                    state = TNTState.EXPLODING;
                    explosionTimer = 0;
                }
                break;

            case EXPLODING: 
                explode(player);
                explosionTimer += deltaMs; 
                if (explosionTimer >= EntityConfig.TNT_EXPLOSION_DURATION) {
                    state = TNTState.EXPLODED;
                }
                break;

            case EXPLODED: {
                //no more updates
                break;
            }
        }
    }
    //--------------------------------------------------------------
    /**
     * Simple "AI" methods to wander around the map
     *  Move randomly up and down, left and right, and stay still
     *  by a random number for TNT_MOVEINTERVAL
     */
    //--------------------------------------------------------------
    private void wander(double deltaMs) {

        double deltaTime = deltaMs / 1000.0; // Convert ms to seconds for speed calculation
        moveTimer += deltaMs; // Increment the timer by the elapsed time
        
        // Change direction at intervals
        if (moveTimer >= EntityConfig.TNT_MOVEINTERVAL) {
            int dir = random.nextInt(5); //Random direction

            switch (dir) {
                case 0 -> {dirX = 0; dirY = 0;} // still
                case 1 -> {dirX = 0; dirY = -1;} // up
                case 2 -> {dirX = 0; dirY = 1;} // down
                case 3 -> {dirX = -1; dirY = 0;} // left
                case 4 -> {dirX = 1; dirY = 0;} // right
            }
            moveTimer = 0; // Reset timer after changing direction
        }
 
        dx = (int) Math.round(dirX *speed * deltaTime);
        dy = (int) Math.round(dirY * speed * deltaTime);

    }
    //-------------------------------------------------------------
    /**
     *Checks if the player is within the detection radius
     * and triggers the TNT if so
     */
    //-------------------------------------------------------------
    private void checkPlayerProximity(Player player) {
        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < EntityConfig.TNT_DETECTION_RADIUS) {
            state = TNTState.TRIGGERED;
            triggerTimer = 0;
        }
    }
    //-------------------------------------------------------------
    /**
     * Handles the explosion logic, damaging the player if within the explosion radius
     */
    //--------------------------------------------------------------
    private void explode(Player player) {
        if (hasDealtDamage) return; // Ensure damage is applied only once per explosion

        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < EntityConfig.TNT_EXPLOSION_RADIUS) {
            player.takeDamage();
            hasDealtDamage = true; // Set flag to prevent further damage
        }
    }
    //-------------------------------------------------------------
    //end update -------------------------------------------------------------


    // GETTERS
    //-------------------------------------------------------------
    public TNTState getState() {
        return state;
    }
    public boolean isExploded() {
        return state==TNTState.EXPLODED;
    }
    //-------------------------------------------------------------
    //SETTERS
    /**
     *  Method to apply damage to the TNT
     */
    //-------------------------------------------------------------
    public void takeDamage() {
        if (state == TNTState.EXPLODED) return; // Already exploded, no further damage
        life--;
        state = TNTState.HIT;
        if (life <= 0) {
            state = TNTState.EXPLODED;
        }
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
