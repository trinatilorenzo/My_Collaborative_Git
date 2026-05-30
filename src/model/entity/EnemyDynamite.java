package model.entity;
import java.awt.Rectangle;
import java.util.List;
import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.DynamiteState;


/**
 * The EnemyDynamite CLASS an NPC that trows dynamite at the player and follow him
 */
//-------------------------------------------------------------------------------------------------------------------
public class EnemyDynamite extends Entity {

    //state
    private DynamiteState state;

    //movement
    private Direction facingDirection;
    private double moveTimer; // Timer to control wandering movement
    private double attackCooldownMs;
    private double dirX =  0, dirY = 0;

    //weapon
    private final List<DynamiteProjectile> globalProjectiles;
    private int attackCount;


    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public EnemyDynamite(SpawnPoint spawnPoint, EntityConfig entityConfig, List<DynamiteProjectile> globalProjectiles) {
        super(entityConfig);

        initializeDefaultValues(spawnPoint);
        this.globalProjectiles = globalProjectiles;
    }
    //-------------------------------------------------------------
    public void initializeDefaultValues(SpawnPoint spawnPoint){
        this.state = EntityConfig.DYNAMITE_DEFAULT_STATE;

        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();

        this.speed = EntityConfig.START_DYNAMITE_SPEED;
        this.life = EntityConfig.DYNAMITE_MAX_LIFE;

        solidArea = new Rectangle(0, 0, EntityConfig.DYNAMITE_HITBOX_WIDTH, EntityConfig.DYNAMITE_HITBOX_HEIGHT);

        this.facingDirection = EntityConfig.START_FACING;
        this.moveTimer=0;
        this.attackCooldownMs = 0;
        this.attackCount = 0;
    }
    //-------------------------------------------------------------


    /**
     * Updates the enemy state and movement
     */
    //------------------------------------------------------------------------
    public void update(Player player, double deltaMs) {
        super.update(); // Reset movement and collision states

        if (attackCooldownMs > 0) {
            attackCooldownMs -= deltaMs;
        }

        checkPlayerProximity(player);

        switch (state) {
            case DEAD:
                System.out.println("Dynamite DEAD");
                break;
            case WANDER:
                wander(deltaMs);
                facePlayer(player);

                break;

            case CHASING:
                chasePlayer(player, deltaMs);
                facePlayer(player);

                break;
            
            case ATTACKING:
                facePlayer(player);
                if (attackCooldownMs <= 0) {
                    attack(player);
                    attackCooldownMs = EntityConfig.DYNAMITE_ATTACK_INTERVAL;
                }
                break;

        }
    }
    //-------------------------------------------------------------
    /**
     *  Simple "AI" methods to wander around the map
     *  Move randomly up and down, left and right, and stay still
     *  by a random number for TNT_MOVEINTERVAL
     */
    private void wander(double deltaMs) {
        //save the current direction

        moveTimer += deltaMs;

        if (moveTimer >= EntityConfig.DYNAMITE_MOVEINTERVAL) {
            // The direction is a random position in a circle radius 1;
            double angle = Math.random() * 2 * Math.PI;
            dirX = Math.cos(angle);
            dirY = Math.sin(angle);
            moveTimer = 0;
        }
        setMove(dirX,dirY,deltaMs);

    }
    //-------------------------------------------------------------
    /**
     * Checks if the player is within the detection radius
     * and triggers the attack or the chase state if so
     */
    private void checkPlayerProximity(Player player) {
        long distanceX = player.worldX - worldX;
        long distanceY = player.worldY - worldY;
        long distanceSq = distanceX * distanceX + distanceY * distanceY;

        double attackRadSq = (double) EntityConfig.DYNAMITE_ATTACKING_RADIUS * EntityConfig.DYNAMITE_ATTACKING_RADIUS;
        double detectRadSq = (double) EntityConfig.DYNAMITE_DETECTION_RADIUS * EntityConfig.DYNAMITE_DETECTION_RADIUS;
        
        if (distanceSq < attackRadSq) {
            state = DynamiteState.ATTACKING;
        } else if (distanceSq < detectRadSq){
            state = DynamiteState.CHASING;
        } else {
            state = DynamiteState.WANDER;
        }
    }
    //-------------------------------------------------------------
    /**
     * Set the direction to follow the player
     */
    private void chasePlayer(Player player, double deltaMs) {
        // distance from the player
        double dxPlayer = player.getWorldX() - worldX; //distance in x
        double dyPlayer = player.getWorldY() - worldY; //distance in y
        double distance = Math.sqrt(dxPlayer * dxPlayer + dyPlayer * dyPlayer);

        if (distance > 0) {
            //normalization to convert the distance to a direction
            dirX = (dxPlayer / distance);
            dirY = (dyPlayer / distance);
        }

        setMove(dirX,dirY,deltaMs);

    }
    //-------------------------------------------------------------
    private void facePlayer(Player player) {
        if (player.getWorldX() >= worldX ){
            facingDirection = Direction.RIGHT;
        }else {
            facingDirection = Direction.LEFT;
        }
    }
    //-------------------------------------------------------------
    private void setMove(double dirX, double dirY, double deltaMs){
        double dist = speed * (deltaMs/1000.0);

        dx = (int) Math.round(dirX * dist);
        dy = (int) Math.round(dirY * dist);

    }
    //-------------------------------------------------------------
    /**
     * Attack by launching a projectile towards the player
     */
    private void attack(Player player) {
        if (player.isDying() || player.isDead()) return; // no attack a dead player

        int distanceX = player.getWorldX() - worldX;
        int distanceY = player.getWorldY() - worldY;
        double distanceSquared = (double) distanceX * distanceX + (double) distanceY * distanceY;
        double maxRadiusSquared = (double) EntityConfig.DYNAMITE_ATTACKING_RADIUS * EntityConfig.DYNAMITE_ATTACKING_RADIUS;
        if (distanceSquared > maxRadiusSquared) {
            return;
        }
        DynamiteProjectile proj = new DynamiteProjectile(
                worldX,
                worldY,
                player.getWorldX(),
                player.getWorldY(),
                player.getCurrentLayer(),
                entityConfig
        );
        globalProjectiles.add(proj);
        attackCount++;
    }
    // end update -------------------------------------------------------------
    
    /**
     *  Method to apply damage to the Entity
     */
    public void takeDamage() {
        if (state == DynamiteState.DEAD) return; // Already dead
        life --;
        if (life <= 0) {
            state = DynamiteState.DEAD;
        }
    }
    //-------------------------------------------------------------


    //GETTER
    //-------------------------------------------------------------
    public DynamiteState getState(){
        return state;
    }
    public boolean isDead(){
        return state==DynamiteState.DEAD;
    }
    public boolean isFacingRight(){
        return facingDirection==Direction.RIGHT;
    }
    public int getAttackCount() {
        return attackCount;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
