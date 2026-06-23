package tinyswordsisland.view.renderer.entity;

import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.model.enu.DynamiteState;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.view.animation.Animation;
import tinyswordsisland.view.animation.AnimationManager;
import tinyswordsisland.view.SpriteLoader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The DynamiteRender CLASS is responsible for rendering the Dynamite entity onto the game screen.
 */
//-------------------------------------------------------------------------------------------------------------------
public class DynamiteRender {
    private final ConcurrentHashMap<IRenderable, AnimationManager> managerByEnemy;
    private final ConcurrentHashMap<IRenderable, Integer> lastAttackCountByEnemy;
    private final ConcurrentHashMap<IRenderable, Boolean> attackAnimationActiveByEnemy;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] attackFrames;

    private BufferedImage[] projectileFrames;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public DynamiteRender(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
        managerByEnemy = new ConcurrentHashMap<>();

        lastAttackCountByEnemy = new ConcurrentHashMap<>();
        attackAnimationActiveByEnemy = new ConcurrentHashMap<>();
    }

    //-------------------------------------------------------------
    private void loadAnimations() {
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/TNT_Red.png");
        BufferedImage projectileImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/Dynamite.png");
        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        attackFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);

        projectileFrames = SpriteLoader.getAnimationFrames(projectileImage, 0, 1, 6, entityConfig.PROJECTILE_SPRITE_WIDTH, entityConfig.PROJECTILE_SPRITE_HEIGHT);
    }

    /**
     * An animation Manger for each enemy entity
     */
    //--------------------------------------------------------------
    private AnimationManager getManager(IRenderable dynamite) {
        return managerByEnemy.computeIfAbsent(dynamite, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("wander", new Animation(wanderFrames, 90, true));
            manager.addAnimation("attack", new Animation(attackFrames, 100, false));
            manager.playAnimation("wander"); // Stato iniziale
            return manager;
        });
    }
    //-------------------------------------------------------------

    /**
     * Change the enemy animation based on his state
     */
    //-------------------------------------------------------------
    public void update(IRenderable enemy, double deltaMs) {
        AnimationManager manager = getManager(enemy);

        switch (DynamiteState.values()[enemy.getRenderState()]) {
            case WANDER, CHASING -> manager.playAnimation("wander");
            case ATTACKING -> manager.playAnimation("attack");
            case DEAD -> removeEnemy(enemy);
        }
        manager.update(deltaMs);
    }
    //-------------------------------------------------------------

    /**
     * Draw the enemy on the screen
     */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, IRenderable enemy, int screenX, int screenY) {
        AnimationManager manager = getManager(enemy);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();

        int width = EntityConfig.DYNAMITE_SPRITE_WIDTH;
        int height = entityConfig.DYNAMITE_SPRITE_HEIGHT;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        if (!enemy.isFacingRightRender()){
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        // DYNAMIC HEALTH BAR (Appears only after taking the first hit)
        if (enemy.getLifeRender() < enemy.getMaxLifeRender()) {
            int barWidth = width/2 ;
            int barHeight = 6;
            int barX = screenX - barWidth / 2;
            int barY = drawY - barHeight - 5;

            double lifePercent = (double) enemy.getLifeRender() / enemy.getMaxLifeRender();
            if (lifePercent < 0) lifePercent = 0;

            int currentBarWidth = (int) (barWidth * lifePercent);
            int roundness = 5;
            // Background (Black outline box)
            g2.setColor(Color.gray);
            g2.fillRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);

            // Health fill (Fiery Orange/Red color)
            g2.setColor(new Color(255, 69, 0));
            g2.fillRoundRect(barX, barY, currentBarWidth, barHeight, roundness, roundness);

            // Border trim
            g2.setColor(new Color(30, 30, 30));
            g2.drawRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);
        }
    }
    //-------------------------------------------------------------
    public void drawProjectile(Graphics2D g2, IRenderable proj, int screenX, int screenY) {
        BufferedImage frame = projectileFrames[0]; // for further use

        int w = entityConfig.PROJECTILE_SPRITE_WIDTH;
        int h = entityConfig.PROJECTILE_SPRITE_HEIGHT;
        double angle = proj.getRenderAngle();

        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(screenX, screenY);
        g2.rotate(angle);
        g2.drawImage(frame, -w / 2, -h / 2, w, h, null);
        g2.setTransform(old);
    }
    //-------------------------------------------------------------

    /**
     * Use to be sure to remove an enemy from render when it's exploded
     */
    //-------------------------------------------------------------
    public void removeEnemy(IRenderable enemy) {
        managerByEnemy.remove(enemy);
        lastAttackCountByEnemy.remove(enemy);
        attackAnimationActiveByEnemy.remove(enemy);
    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the tnt's solid area and interaction radius for the mob and the projectile.
     */
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, IRenderable enemyDynamite, int screenX, int screenY) {
        Rectangle solid = enemyDynamite.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // Attack Radius
        g2.setColor(new Color(93, 255, 0, 80));
        int r = entityConfig.DYNAMITE_ATTACKING_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.GREEN);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);

        // Detection Radius
        g2.setColor(new Color(0, 84, 255, 80));
        r = entityConfig.DYNAMITE_DETECTION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);
    }
    public void drawProjectileSolidArea(Graphics2D g2, IRenderable proj, int screenX, int screenY) {
        Rectangle solid = proj.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 165, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.ORANGE);
        g2.drawRect(drawX, drawY, solid.width, solid.height);
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
