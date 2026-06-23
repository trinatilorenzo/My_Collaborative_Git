package tinyswordsisland.model.entity;

import java.awt.Rectangle;

import tinyswordsisland.config.EntityConfig;

/**
 * Projectile launched by EnemyDynamite.
 */
//----------------------------------------------------------------------------------------------------------------------
public class DynamiteProjectile extends Entity {

    //Physics
    private static final double GRAVITY = 800.0; // px/s^2

    //movement
    private final double velocityX;
    private final double initialVelocityY;
    private double angle;

    private final int launchX;
    private final int launchY;
    private final int targetX;
    private final int targetY;
    private final double flightTime;

    //state
    private boolean exploded;
    private double timer;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public DynamiteProjectile(int startX, int startY, int targetX, int targetY, int layer, EntityConfig entityConfig) {
        super(entityConfig);

        this.worldX = startX;
        this.worldY = startY;

        this.launchX = worldX;
        this.launchY = worldY;

        this.targetX = targetX;
        this.targetY = targetY;
        this.currentLayer = layer;

        this.solidArea = new Rectangle(0, 0, EntityConfig.PROJECTILE_SIZE, EntityConfig.PROJECTILE_SIZE);

        this.flightTime = EntityConfig.PROJECTILE_AIR_TIME / 1000.0;

        double dx = targetX - worldX;
        double dy = targetY - worldY;

        this.velocityX = dx / flightTime;
        this.initialVelocityY = (dy - 0.5 * GRAVITY * flightTime * flightTime) / flightTime;
    }
    //-------------------------------------------------------------


    /**
     * Update the projectile's position and state
     * use the basic parabolic motion folmulas to move the projectile
     */
    //-------------------------------------------------------------
    @Override
    public void update(double deltaMs) {
        if (exploded) return;

        timer = Math.min(timer + deltaMs / 1000.0, flightTime);

        // x = vx*t
        worldX = launchX + (int) Math.round(velocityX * timer);
        // y = vy0*t + 1/2*a*t^2
        worldY = launchY + (int) Math.round(initialVelocityY * timer + 0.5 * GRAVITY * timer * timer);
        // angle = atan2(vy,vx)
        double velocityY = initialVelocityY + GRAVITY * timer;
        angle = Math.atan2(velocityY, velocityX);

        if (timer >= flightTime) {
            // projectile has reached the target cause in
            // flight time interval should reach the target

            // be sure tho set the final position
            worldX = targetX;
            worldY = targetY;

            // is the end time to explode
            explode();
        }

    }
    //-------------------------------------------------------------

    //GETTER
    //-------------------------------------------------------------
    public boolean isExploded() {
        return exploded;
    }
    public double getAngle() {
        return angle;
    }
    //-------------------------------------------------------------

    //SETTER
    //-------------------------------------------------------------
    public void explode() {
        exploded = true;
    }
    //-------------------------------------------------------------

}
//----------------------------------------------------------------------------------------------------------------------
