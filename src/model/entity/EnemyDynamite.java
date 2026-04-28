package model.entity;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.DynamiteState;

public class EnemyDynamite extends Entity {

    private DynamiteState state = DynamiteState.WANDER;
    private List<DynamiteProjectile> globalProjectiles;
    private EntityConfig entityConfig;
    private double attackTimer = 0;
    private double moveTimer = 0; // Timer to control wandering movement

    private double dirX = 0; //save the current direction of TNT
    private double dirY = 0;

    private final double moveInterval = 1000; // Change direction every 1 second

    private double detectionRadius; // Radius within which the dynamite detects the player

    private boolean facingRight = true;
    
    //CONSTRUCTOR
    public EnemyDynamite(SpawnPoint spawnPoint, EntityConfig entityConfig, List<DynamiteProjectile> globalProjectiles) {
        this.entityConfig = entityConfig;
        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = 2; //TODO: set well

        this.speed = entityConfig.DYNAMITE_SPEED;
        this.detectionRadius = entityConfig.DYNAMITE_DETECTION_RADIUS;

        solidArea = new Rectangle(0, 0, entityConfig.DYNAMITE_HITBOX_WIDTH, entityConfig.DYNAMITE_HITBOX_HEIGHT);
    }

    public void update(Player player, double deltaMs) {
        super.update(); // Reset movement and collision states
        switch (state) {
            case WANDER:
                wander(deltaMs);
                checkPlayerProximity(player);
                break;

            case CHASING:
                chasePlayer(player, deltaMs);
                checkPlayerProximity(player);
                break;
            
            case ATTACKING:
                attackTimer += deltaMs;
                if (attackTimer >= entityConfig.DYNAMITE_ATTACK_INTERVAL) { // Attack every 2 seconds
                    attack(player);
                    attackTimer = 0;
                }
                checkPlayerProximity(player);
                break;
            
        }
        
        
    }  

    //-------------------------------------------------------------------------------
    // CHeck if the player is near the enemy
    private void checkPlayerProximity(Player player) {
        int distanceX = player.worldX - worldX;
        int distanceY = player.worldY - worldY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if (distance < entityConfig.DYNAMITE_ATTACKING_RADIUS) {
            state = DynamiteState.ATTACKING;
            //triggerTimer = 0;
        } else if (distance < entityConfig.DYNAMITE_DETECTION_RADIUS){
            state = DynamiteState.CHASING;
        } else {
            state = DynamiteState.WANDER;
        }
    }
    
    //-------------------------------------------------------------------------------
    /* Wander randomly within a small area */
    private void wander(double deltaMs) {
        // Simple random movement logic 
        moveTimer += deltaMs;

        if (moveTimer >= moveInterval) {
            double angle = Math.random() * 2 * Math.PI;
            dirX = Math.cos(angle);
            dirY = Math.sin(angle);
            moveTimer = 0;
        }
        double dist = speed * (deltaMs / 1000.0);

        dx = (int) Math.round(dirX * dist);
        dy = (int) Math.round(dirY * dist);

        facingRight = dirX >= 0;

    }

    //-------------------------------------------------------------------------------
    /* Chase the player */
    private void chasePlayer(Player player, double deltaMs) {
        double dxPlayer = player.getWorldX() - worldX;
        double dyPlayer = player.getWorldY() - worldY;
        double distance = Math.sqrt(dxPlayer * dxPlayer + dyPlayer * dyPlayer);

        if (distance > 0) {
            dirX = (dxPlayer / distance); //normalization
            dirY = (dyPlayer / distance);
        }

        double dist = speed * (deltaMs/1000.0);

        dx = (int) Math.round(dirX * dist);
        dy = (int) Math.round(dirY * dist);

        facingRight = dirX>=0;
        
    }

    /* Attack by launching a projectile towards the player */
    private void attack(Player player) {
        DynamiteProjectile proj = new DynamiteProjectile(worldX, worldY, player.getWorldX(), player.getWorldY(), entityConfig);
        globalProjectiles.add(proj);
    }

    //GETTER
    public DynamiteState getState(){
        return state;
    }
    public boolean isDead(){
        return state==DynamiteState.DEAD;
    }
    public boolean isFacingRight(){
        return facingRight;
    }
    
}
