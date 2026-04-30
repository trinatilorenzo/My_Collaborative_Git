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
    private final double flightTime = 1.2; // Il tempo (in secondi) che la dinamite impiega per colpire il punto target

    private EntityConfig entityConfig;

    //CONSTRUCTOR
    public DynamiteProjectile(double startX, double startY, double targetX, double targetY, EntityConfig entityConfig){
        this.worldXDouble = startX;
        this.worldYDouble = startY;
        
        this.worldX = (int) startX;
        this.worldY = (int) startY;

        this.currentLayer = 2; 

        this.entityConfig = entityConfig;
        
        this.solidArea = new Rectangle(0, 0, entityConfig.PROJECTILE_SIZE, entityConfig.PROJECTILE_SIZE); //TODO: aggiustare 

        double dx = targetX - startX;
        double dy = targetY - startY;

        this.velocityX = dx / flightTime;
        this.velocityY = (dy - 0.5 * gravity * (flightTime * flightTime)) / flightTime;

    }

    //-------------------------------------------------------------------------------------------------
    public void update(double deltaMs) {

        if (exploded) return;

        double dt = deltaMs / 1000.0;
        timer += dt;

        // gravità
        velocityY += gravity * dt;

        // movimento
        worldXDouble += velocityX * dt;
        worldYDouble += velocityY * dt;

        angle = Math.atan2(velocityY, velocityX);

        this.worldX = (int)worldXDouble;
        this.worldY = (int)worldYDouble;

        // esplosione a tempo
        if (timer > flightTime) {
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
