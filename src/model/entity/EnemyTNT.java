package model.entity;

import java.awt.*;
import java.util.Random;

import main.CONFIG.EntityConfig;
import main.ENUM.TNTState;

public class EnemyTNT extends Entity{
    private TNTState state;
    private int hp = 1;
    private long triggerTime;
    private long explosionDelay = 2000;

    private final int detectionRadius = 100; // Example radius for detecting the player
    private final int explosionRadius = 50; // Example radius for explosion damage

    private EntityConfig entityConfig;
    private Random random = new Random();
    private long lastMoveChange = EntityConfig.START_TNT_SPEED;
    private long moveChangeInterval = EntityConfig.START_TNT_SPEED; // Change direction every second

    public EnemyTNT(int worldX, int worldY, EntityConfig entityConfig) {
        
        this.entityConfig = entityConfig;
        this.worldX = worldX;
        this.worldY = worldY;
        this.state = TNTState.WANDER;

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
                if (System.currentTimeMillis() - triggerTime > explosionDelay) {
                    explode(player);
                    state = TNTState.EXPLODED;
                }
                break;
            case EXPLODING: 
                state = TNTState.EXPLODED;
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
        long now = System.currentTimeMillis();

        if (now - lastMoveChange > moveChangeInterval) {
            intendedDx = 0;
            intendedDy = 0;

            int dir = random.nextInt(4);

            switch (dir) { // 0: up, 1: down, 2: left, 3: right
                case 0 -> intendedDy = -speed;
                case 1 -> intendedDy = speed;
                case 2 -> intendedDx = -speed;
                case 3 -> intendedDx = speed;
            }

            lastMoveChange = now;
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
            triggerTime = System.currentTimeMillis();
        }
    }

    //--------------------------------------------------------------
    // Handles the explosion logic, e.g., damaging the player if within the explosion radius
    private void explode(Player player) {
        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < explosionRadius) {
            // TODO: Implement explosion damage logic
            // e.g., player.takeDamage();
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


