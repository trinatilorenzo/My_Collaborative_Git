package model.entity;

import java.awt.Rectangle;

import main.CONFIG.EntityConfig;

public class DynamiteProjectile extends Entity{
    private double worldXDouble, worldYDouble;
    private double velocityX, velocityY;
    private double angle = 0;

    private double gravity = 800; // px/s^2

    private boolean exploded = false;

    private double timer = 0;

    private EntityConfig entityConfig;

    //CONSTRUCTOR
    public DynamiteProjectile(double startX, double startY, double targetX, double targetY, EntityConfig entityConfig){
        this.worldXDouble = startX;
        this.worldYDouble = startY;
        
        this.worldX = (int) startX;
        this.worldY = (int) startY;

        this.currentLayer = 2; 

        this.entityConfig = entityConfig;
        
        this.solidArea = new Rectangle(worldX, worldY, entityConfig.PROJECTILE_SIZE, entityConfig.PROJECTILE_SIZE); //TODO: aggiustare 

        double dx = targetX - startX;
        double dy = targetY - startY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.01){
            dx /= distance;
            dy /= distance;
        }

        velocityX = dx * entityConfig.PROJECTILE_THROW_SPEED;
        velocityY = -entityConfig.PROJECTILE_THROW_HEIGHT;

    }

    //-------------------------------------------------------------------------------------------------
    public void update(double deltaMs) {

        if (exploded) return;

        double dt = deltaMs / 1000.0;

        // gravità
        velocityY += gravity * dt;

        // movimento
        worldXDouble += velocityX * dt;
        worldYDouble += velocityY * dt;
        angle = Math.atan2(velocityY, velocityX);

        //sync con entity
        solidArea.x = worldX;
        solidArea.y = worldY;

        // esplosione a tempo
        timer += deltaMs;
        if (timer > entityConfig.PROJECTILE_FUSE_TIME) {
            explode();
        }
    }

    public void explode() {
        exploded = true;
    }

    //-----------------------------------------------------------------------
    //GETTER
    public boolean isExploded(){
        return exploded;
    }
    public double getAngle(){
        return angle;
    }
}
