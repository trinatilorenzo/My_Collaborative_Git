package model.entity;

import java.awt.Rectangle;
import java.util.Random;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.TNTState;

public class EnemyTNT extends Entity{
    private TNTState state = TNTState.WANDER;
    private int hp = 1;

    private long triggerTimer;
    private long explosionTimer;

    private long explosionDelay = 2000; // Time in milliseconds between being triggered and exploding

    private final int detectionRadius = 100; // Example radius for detecting the player
    private final int explosionRadius = 50; // Example radius for explosion damage

    private double dirX = 0; //save the current direction of TNT
    private double dirY = 0;

    private EntityConfig entityConfig;

    private Random random = new Random();

    private double moveTimer = 0; // Timer to control wandering movement
    private final double moveInterval = 1000; // Change direction every 1 second
    private final int EXPLOSION_DURATION = 300;

    public EnemyTNT(int worldX, int worldY, EntityConfig entityConfig) {
        
        this.entityConfig = entityConfig;
        this.worldX = worldX;
        this.worldY = worldY;
        this.currentLayer = entityConfig.ENEMY_TNT_LAYER;
        this.speed = entityConfig.START_TNT_SPEED;

        solidArea = new Rectangle((entityConfig.TNT_SPRITE_WIDTH / 2) - (entityConfig.TNT_HITBOX_WIDTH/2),
                (entityConfig.TNT_SPRITE_HEIGHT / 2),
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
                triggerTimer += deltaMs;
                if (triggerTimer >= explosionDelay) {
                    state = TNTState.EXPLODING;
                    explosionTimer = 0;
                }
                break;

            case EXPLODING: 
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
        double deltaTime = deltaMs / 1000.0; // Convert ms to seconds for speed calculation
        moveTimer += deltaMs; // Increment the timer by the elapsed time
        
        // Change direction at intervals
        if (moveTimer >= moveInterval) { 
            int dir = random.nextInt(4); //Random direction

            switch (dir) { // 0: up, 1: down, 2: left, 3: right
                case 0 -> {dirX = 0; dirY = -1;}
                case 1 -> {dirX = 0; dirY = 1;}
                case 2 -> {dirX = -1; dirY = 0;}
                case 3 -> {dirX = 1; dirY = 0;}
            }
            moveTimer = 0; // Reset timer after changing direction
        }
 
        dx = (int) Math.round(dirX *speed * deltaTime);
        dy = (int) Math.round(dirY * speed * deltaTime);

        intendedDx = dx;
        intendedDy = dy;
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


