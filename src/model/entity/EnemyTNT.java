package model.entity;

import java.awt.Rectangle;
import java.util.Random;

import main.CONFIG.EntityConfig;
import main.ENUM.TNTState;

public class EnemyTNT extends Entity{
    private TNTState state = TNTState.WANDER;
    private int hp = 1;

    private long triggerTimer;
    private long explosionTimer;

    private long explosionDelay = 2000; // Time in milliseconds between being triggered and exploding

    private final int detectionRadius = 100; // Example radius for detecting the player
    private final int explosionRadius = 50; // Example radius for explosion damage

    private EntityConfig entityConfig;

    private Random random = new Random();

    private double moveTimer = 0; // Timer to control wandering movement
    private final double moveInterval = 1000; // Change direction every 1 second
    private final int EXPLOSION_DURATION = 300;

    public EnemyTNT(int worldX, int worldY, EntityConfig entityConfig) {
        
        this.entityConfig = entityConfig;
        this.worldX = worldX;
        this.worldY = worldY;

        this.speed = entityConfig.START_TNT_SPEED;

        solidArea = new Rectangle((entityConfig.TNT_SPRITE_WIDTH / 2) - (entityConfig.TNT_HITBOX_WIDTH/2),
                (entityConfig.TNT_SPRITE_HEIGHT / 2) ,
                entityConfig.TNT_HITBOX_WIDTH,
                entityConfig.TNT_HITBOX_HEIGHT);

    }

    public void update(Player player, double deltaMs) {
        super.update(); // Reset movement and collision states
        switch (state) {
            case WANDER: 
                wander(deltaMs);
                checkPlayerProximity(player);
                break;
            case TRIGGERED: 
                dx = 0;
                dy = 0;
                triggerTimer += deltaMs;
                if (triggerTimer >= explosionDelay) {
                    state = TNTState.EXPLODING;
                    explosionTimer = 0;
                }
                break;


            case EXPLODING: 
                dx = 0;
                dy = 0;
                explode(player);
                explosionTimer += deltaMs; 
                if (explosionTimer >= EXPLOSION_DURATION) {
                    state = TNTState.EXPLODED;
                }
                break;
            case EXPLODED: {
                // TODO: Handle post-explosion logic, e.g., remove from game
                break;
            }
        }
    }

    //--------------------------------------------------------------
    // Simple wandering behavior: changes direction at set intervals
    private void wander(double deltaMs) {
        moveTimer += deltaMs;

        if (moveTimer >= moveInterval) {
            intendedDx = 0;
            intendedDy = 0;

            int dir = random.nextInt(4);

            switch (dir) { // 0: up, 1: down, 2: left, 3: right
                case 0 -> intendedDy = -speed;
                case 1 -> intendedDy = speed;
                case 2 -> intendedDx = -speed;
                case 3 -> intendedDx = speed;
            }

            moveTimer = 0;
        }

        dx = intendedDx;
        dy = intendedDy;
    
        }
    
    //-------------------------------------------------------------
    // Checks if the player is within the detection radius and triggers the TNT if so
    private void checkPlayerProximity(Player player) {
        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;

        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < detectionRadius) {
            state = TNTState.TRIGGERED;
            triggerTimer = 0;
        }
    }

    //--------------------------------------------------------------
    // Handles the explosion logic, damaging the player if within the explosion radius
    private void explode(Player player) {
        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;
        
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < explosionRadius) {
            player.takeDamage(10);
        }
    }

    //-------------------------------------------------------------
    // Method to apply damage to the TNT, potentially triggering an explosion
    public void takeDamage(int damage) {
        if (state == TNTState.EXPLODED) return; // Already exploded, no further damage
        hp -= damage;
        if (hp <= 0) {
            state = TNTState.EXPLODED;
        }
    }

// GETTERS
    public TNTState getState() {
        return state;
    }
}


